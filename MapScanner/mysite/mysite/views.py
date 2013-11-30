#-*- coding:utf-8 -*-

from django.shortcuts import get_object_or_404, render_to_response, redirect
from django.http import Http404, HttpRequest, HttpResponse
from django.template import RequestContext

import MySQLdb

class TableKeyRelationship:
    '''mapping <table1, table2> with structure table1 join table2 on key1 = key2'''
    table_name_ori = ""
    table_name_rel = ""
    key_column = ""
    def __init__(self,table_name_ori_i, table_name_rel_i, key_column):
        self.table_name_ori = table_name_ori_i        
        self.table_name_rel = table_name_rel_i
        self.key_column = key_column
        
class TableInfo:
    child_list = []
    column_list = []
    name = ""
    def __init__(self, child_list_i, key_list_i, name_i):
        child_list = child_list_i
        key_list = key_list_i
        name = name_i

def index(request):
    return HttpResponse('Hello Map')

def map(request, map_id):
    # initial table relationship
    table_relationship = _init_table_structure()
    
    # get info from POST method
    
    # get info from db
    result = _select(map_id, table_relationship)
    
    # construct info into url
    hyperlink = _get_link(result)
    #hyperlink = "http://api.map.baidu.com/staticimage?width=400&height=200&center=北京&markers=百度大厦|116.403874,39.914888&zoom=10&markerStyles=s,A,0xff0000"
        
    #return HttpResponse('Hello ' + map_id)
    return render_to_response('map.html',{'link':hyperlink})

def _connect_sql():
    return MySQLdb.connect(host='localhost', user='root', passwd='feixuluohua', db='rent_info', port=3306, charset='gb2312')

def _select(map_id, table_relationship):
    try:
        conn = _connect_sql()
        cur = conn.cursor()
          
        #cur.execute('select * from mlu_city')
        cur.execute('select a11.HOUSE_RENT_INFO_NAME as HN, a11.RENT_MONEY as MONEY, a11.RENT_SIZE as SIZE , a14.DISTRICT_NAME as DISTRICT, a15.ROOM_ROOM_NUM ROOM_NUM, a15.ROOM_SPACE_NUM SPACE_NUM from house_rent_info a11 join mlu_detail_local a12 on a11.MLU_DETAIL_LOCAL_DETAIL_LOCAL_ID = a12.DETAIL_LOCAL_ID join mlu_local a13 on a12.MLU_LOCAL_LOCAL_ID = a13.LOCAL_ID join mlu_district a14 on  a13.MLU_DISTRICT_DISTRICT_ID = a14.DISTRICT_ID join mlu_room_type a15 on  a11.MLU_ROOM_TYPE_ROOM_TYPE_ID = a15.ROOM_TYPE_ID where a11.RENT_MONEY < 3000 and a15.ROOM_ROOM_NUM <= 2 and a15.ROOM_SPACE_NUM >=1 limit 10;')
        result = cur.fetchall()
        
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

def _init_table_structure():
    table_relationship = []
    
    city_relationship = TableKeyRelationship('mlu_city','mlu_district','CITY_ID')
    child_list = []
    column_list = []
    child_list.append(city_relationship)
    column_list.append('CITY_ID')
    column_list.append('CITY_NAME')
    city_table = TableInfo(child_list,column_list,'mlu_city')