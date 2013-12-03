#-*- coding:utf-8 -*-

from django.shortcuts import get_object_or_404, render_to_response, redirect
from django.http import Http404, HttpRequest, HttpResponse
from django.template import RequestContext

import MySQLdb

class TableInfo:
    def __init__(self, child_list_i, key_list_i, name_i):
        self.child_list = child_list_i
        self.key_list = key_list_i
        self.name = name_i

class TableKeyRelationship:
    '''mapping <table1, table2> with structure table1 join rel_table on key_column
    table1 is source table which call this relationship'''
    def __init__(self,rel_table_i, key_column_i):
        self.rel_table = rel_table_i        
        self.key_column = key_column_i

def index(request):
    return HttpResponse('Hello Map')

def map(request, map_id):
    # initial table relationship
    table_relationship = _init_table_structure()
    
    # get info from POST method
    attr_list = []
    
    # get info from db
    result = _select(map_id, attr_list, table_relationship)
    
    # construct info into url
    hyperlink = ""
    #hyperlink = _get_link(result)
    #hyperlink = "http://api.map.baidu.com/staticimage?width=400&height=200&center=北京&markers=百度大厦|116.403874,39.914888&zoom=10&markerStyles=s,A,0xff0000"
        
    #return HttpResponse('Hello ' + map_id)
    return render_to_response('map.html',{'link':hyperlink})

def _connect_sql():
    return MySQLdb.connect(host='localhost', user='root', passwd='feixuluohua', db='rent_info', port=3306, charset='gb2312')

def _select(map_id, attr_list, table_relationship):
    try:
        conn = _connect_sql()
        cur = conn.cursor()
          
        #cur.execute('select * from mlu_city')
        #cur.execute('select a11.HOUSE_RENT_INFO_NAME as HN, a11.RENT_MONEY as MONEY, a11.RENT_SIZE as SIZE , a14.DISTRICT_NAME as DISTRICT, a15.ROOM_ROOM_NUM ROOM_NUM, a15.ROOM_SPACE_NUM SPACE_NUM from house_rent_info a11 join mlu_detail_local a12 on a11.MLU_DETAIL_LOCAL_DETAIL_LOCAL_ID = a12.DETAIL_LOCAL_ID join mlu_local a13 on a12.MLU_LOCAL_LOCAL_ID = a13.LOCAL_ID join mlu_district a14 on  a13.MLU_DISTRICT_DISTRICT_ID = a14.DISTRICT_ID join mlu_room_type a15 on  a11.MLU_ROOM_TYPE_ROOM_TYPE_ID = a15.ROOM_TYPE_ID where a11.RENT_MONEY < 3000 and a15.ROOM_ROOM_NUM <= 2 and a15.ROOM_SPACE_NUM >=1 limit 10;')
        #result = cur.fetchall() 
        result = _execute(attr_list,table_relationship)
        
        cur.close()
        conn.close()
    
    except MySQLdb.Error, e:
        print 'MySQL Execution Error'    
    
    return result

def _get_link(result):
    basic_link = 'http://api.map.baidu.com/staticimage?'
    width = u'width=' + str(400)
    height = u'&height=' + str(200)
    center = u'&center=' + u'芳草地'
    markers = u'&markers='
    zoom = u'&zoom=' + str(12)
    markerstyles = u'&markerStyles=s,A,0x00ff00'
    
    for row in result:
        markers = markers + row[0] + '|'
    markers = markers[0:len(markers) - 1]
    link = basic_link + width + height + center + markers + zoom + markerstyles
    return link

def _execute(attr_list,table_relationship):
    _find_join_path_table_name("mlu_city","mlu_local",table_relationship)

def _init_table_structure():    
    all_tables = []
    
    child_list = []
    column_list = []
    local_table = TableInfo(child_list,column_list,'mlu_local')
    all_tables.append(local_table)
    
    district_relationship = TableKeyRelationship(local_table,'local_id')
    child_list = []
    column_list = []
    child_list.append(district_relationship)
    column_list.append('district_id')
    column_list.append('district_name')
    district_table = TableInfo(child_list,column_list,'mlu_district')
    all_tables.append(district_table)
    
    city_relationship = TableKeyRelationship(district_table,'city_id')
    child_list = []
    column_list = []
    child_list.append(city_relationship)
    column_list.append('city_id')
    column_list.append('city_name')
    city_table = TableInfo(child_list,column_list,'mlu_city')
    all_tables.append(city_table)

    return all_tables

# find best table to match input attribute
def _find_table(attr_str,all_tables):
    for table in all_tables:
        for attr_table in table.column_list:
            if attr_table == attr_str:
                return table
    return None

def _find_join_path_table_name(table1_name,table2_name,all_tables):
    flag1 = False
    falg2 = False
    for table in all_tables:
        if table.name == table1_name:
            table1 = table
            flag1 = True
            continue
        if table.name == table2_name:
            table2 = table
            flag2 = True
            continue
    result = ""
    if flag1 == True and flag2 == True:
        result = _find_join_path(table1,table2,table1.name + " a0",1)
    print result
    
def _find_join_path(table1,table2,str_path,index):
    if table1 == table2:
        return str_path
    
    for sub_table_rel in table1.child_list:        
        str_path = str_path + " join " + sub_table_rel.rel_table.name + " a" + str(index) + " on " + "a" + str(index - 1) + "." + sub_table_rel.key_column + " = " + " a" + str(index) + "." + table1.name + "_" + sub_table_rel.key_column
        return _find_join_path(sub_table_rel.rel_table,table2,str_path,index + 1)   
            