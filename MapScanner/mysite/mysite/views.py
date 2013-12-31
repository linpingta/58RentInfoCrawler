#-*- coding:gb2312 -*-

from django.shortcuts import get_object_or_404, render_to_response, redirect
from django.http import Http404, HttpRequest, HttpResponse
from django.template import RequestContext

import MySQLdb
import urllib
import json

class AttrRel:
    def __init__(self,key_i,alias_i,attrname_i,operator_i,filter_ansewer_i):
        self.key = key_i
        self.filter_answer = filter_ansewer_i
        self.alias = alias_i
        self.attrname = attrname_i
        self.operator = operator_i
        
class TableInfo:
    def __init__(self, child_list_i, key_list_i, name_i, index_i):
        self.child_list = child_list_i
        self.key_list = key_list_i
        self.name = name_i
        self.index = index_i

class TableKeyRelationship:
    '''mapping <table1, table2> with structure table1 join rel_table on key_column
    table1 is source table which call this relationship'''
    def __init__(self,rel_table_i, key_column_i):
        self.rel_table = rel_table_i        
        self.key_column = key_column_i

def index(request):
    return HttpResponse('Hello Map')

def test_map(request):
    #hyperlink = "http://api.map.baidu.com/direction?origin=latlng:34.264642646862,108.95108518068|name:我家&destination=大雁塔&mode=driving&region=西安&output=html"
    hyperlink = "http://api.map.baidu.com/direction/v1/routematrix?output=json&origins=芳草地西街&destinations=朝阳门&ak=OspflLhgVDoR5P57YP6hlBV7"
    data_back = urllib.urlopen(hyperlink).read()
    json_array = json.loads(data_back)
    print json_array
    return render_to_response('test_map.html',{'link':hyperlink})

def map(request, map_id):
    # initial table relationship
    table_relationship = _init_table_structure()
    
    # get info from POST method
    attr_list = []
    attr1 = "mlu_local"
    tabl1_attr1 = _find_table(attr1, table_relationship)
    tmp_attr = AttrRel(attr1," a" + str(tabl1_attr1.index),"local_name","=","'东大桥'")
    attr_list.append(tmp_attr)
    
    # get info from db
    result = _select(map_id, attr_list, table_relationship, "house_rent_info")
            
    # construct info into url
    #hyperlink = ""
    hyperlink = _get_link(result,attr_list)
    #hyperlink = "http://api.map.baidu.com/staticimage?width=400&height=200&center=北京&markers=百度大厦|116.403874,39.914888&zoom=10&markerStyles=s,A,0xff0000"    
    
    #return HttpResponse('Hello ' + map_id)
    return render_to_response('map.html',{'link':hyperlink})

def _connect_sql():
    return MySQLdb.connect(host='localhost', user='root', passwd='feixuluohua', db='rent_info', port=3306, charset='gb2312')

def _select(map_id, attr_list, table_relationship, fact_table):
    try:
        conn = _connect_sql()
        cur = conn.cursor()
        
        result = ""
          
        #cur.execute('select * from mlu_city')
        #cur.execute('select a11.HOUSE_RENT_INFO_NAME as HN, a11.RENT_MONEY as MONEY, a11.RENT_SIZE as SIZE , a14.DISTRICT_NAME as DISTRICT, a15.ROOM_ROOM_NUM ROOM_NUM, a15.ROOM_SPACE_NUM SPACE_NUM from house_rent_info a11 join mlu_detail_local a12 on a11.MLU_DETAIL_LOCAL_DETAIL_LOCAL_ID = a12.DETAIL_LOCAL_ID join mlu_local a13 on a12.MLU_LOCAL_LOCAL_ID = a13.LOCAL_ID join mlu_district a14 on  a13.MLU_DISTRICT_DISTRICT_ID = a14.DISTRICT_ID join mlu_room_type a15 on  a11.MLU_ROOM_TYPE_ROOM_TYPE_ID = a15.ROOM_TYPE_ID where a11.RENT_MONEY < 3000 and a15.ROOM_ROOM_NUM <= 2 and a15.ROOM_SPACE_NUM >=1 limit 10;')
        
        from_where_result = _execute(attr_list,table_relationship, fact_table)
        select_result = "select a11.house_rent_info_name, a11.rent_money, a11.rent_size from "
        all_result = select_result + from_where_result + " order by (a11.rent_money / a11.rent_size) limit 10;"
        print all_result
        
        f = open('test','w')
        f.write(all_result)
        
        cur.execute(all_result)
        result = cur.fetchall()
        
        cur.close()
        conn.close()
    
    except MySQLdb.Error, e:
        print 'MySQL Execution Error'    
    
    return result

def _get_link(result,attr_list):
    basic_link = 'http://api.map.baidu.com/staticimage?'
    width = u'width=' + str(400)
    height = u'&height=' + str(200)
    
    center_place = u'天安门'
    zoom_level = 0
    for attr in attr_list:
        if attr.key == 'mlu_local':
            center_place = attr.filter_answer
            zoom_level = 2
            break
        elif attr.key == 'mlu_district':
            center_place = attr.filter_answer
            zoom_level = 1
    print center_place
    center = u'&center=' + center_place.decode('gb2312')
    
    markers = u'&markers='
    if zoom_level == 0:
        zoom = u'&zoom=' + str(12)
    elif zoom_level == 1:
        zoom = u'&zoom=' + str(12)
    elif zoom_level == 2:
        zoom = u'&zoom=' + str(15)
    print 'zoom_level: ' + zoom
    markerstyles = u'&markerStyles=s,A,0x00ff00'
    
    for row in result:
        print row
        markers = markers + row[0] + '|'
    markers = markers[0:len(markers) - 1]
    link = basic_link + width + height + center + markers + zoom + markerstyles
    print link
    return link

def _execute(attr_list,table_relationship,fact_table):
    str_result_init = "mlu_city" + " a1"
    str_result = _find_join_path_table_name("mlu_city","house_rent_info",str_result_init,table_relationship)
    
    attr_filter_all = "  where "
    
    for attr in attr_list:
        attr_key = attr.key
        attr_tmp_filter = attr.alias + "." + attr.attrname + attr.operator + attr.filter_answer
        attr_filter_all = attr_filter_all + attr_tmp_filter + " and "
        str_result2 = _find_join_path_table_name(attr_key,fact_table,str_result,table_relationship)
        print str_result2
        str_result = str_result2
    
    result = str_result + attr_filter_all[1:len(attr_filter_all) - 5]
    print result
    return result
    
def _init_table_structure():    
    all_tables = []
    
    #agent relationship is MtoM, now ignore it firstly.
    
    child_list = []
    column_list = []
    column_list.append('house_rent_info_id')
    column_list.append('house_rent_info_name')
    column_list.append('direction_type')
    column_list.append('rent_size')
    column_list.append('rent_money')
    house_rent_info = TableInfo(child_list,column_list,'house_rent_info',11)
    all_tables.append(house_rent_info)
    
    house_pay_relationship = TableKeyRelationship(house_rent_info,'pay_type_id')
    child_list = []
    column_list = []
    child_list.append(house_pay_relationship)
    column_list.append('pay_type_id')
    column_list.append('pay_num')
    column_list.append('loan_num')
    pay_type_table = TableInfo(child_list,column_list,'mlu_pay_type',10)
    all_tables.append(pay_type_table)
    
    house_room_relationship = TableKeyRelationship(house_rent_info,'room_type_id')
    child_list = []
    column_list = []
    child_list.append(house_room_relationship)
    column_list.append('room_type_id')
    column_list.append('room_space_num')
    column_list.append('room_room_num')
    room_type_table = TableInfo(child_list,column_list,'mlu_room_type',9)
    all_tables.append(room_type_table)
    
    house_stair_relationship = TableKeyRelationship(house_rent_info,'stair_type_id')
    child_list = []
    column_list = []
    child_list.append(house_stair_relationship)
    column_list.append('stair_type_id')
    column_list.append('cur_stair_num')
    column_list.append('most_stair_num')
    stair_type_table = TableInfo(child_list,column_list,'mlu_stair_type',8)
    all_tables.append(stair_type_table)
    
    house_info_source_relationship = TableKeyRelationship(house_rent_info,'info_source_id')
    child_list = []
    column_list = []
    child_list.append(house_info_source_relationship)
    column_list.append('info_source_id')
    column_list.append('info_srouce_name')
    info_source_table = TableInfo(child_list,column_list,'mlu_info_source',7)
    all_tables.append(info_source_table)
    
    house_detail_relationship = TableKeyRelationship(house_rent_info,'detail_local_id')
    child_list = []
    column_list = []
    child_list.append(house_detail_relationship)
    column_list.append('detail_local_id')
    column_list.append('detail_local_name')
    column_list.append('green_rate')
    column_list.append('open_time')
    detail_local_table = TableInfo(child_list,column_list,'mlu_detail_local',6)
    all_tables.append(detail_local_table)
    
    service_relationship = TableKeyRelationship(detail_local_table,'service_company_id')
    child_list = []
    column_list = []
    child_list.append(service_relationship)
    column_list.append('service_company_id')
    column_list.append('service_company_name')
    service_company_table = TableInfo(child_list,column_list,'lu_service_company',5)
    all_tables.append(service_company_table)
    
    open_company_relationship = TableKeyRelationship(detail_local_table,'open_company_id')
    child_list = []
    column_list = []
    child_list.append(open_company_relationship)
    column_list.append('open_company_id')
    column_list.append('open_company_name')
    open_company_table = TableInfo(child_list,column_list,'mlu_open_company',4)
    all_tables.append(open_company_table)
    
    local_relationship = TableKeyRelationship(detail_local_table,'local_id')
    child_list = []
    column_list = []
    child_list.append(local_relationship)
    column_list.append('local_id')
    column_list.append('local_name')
    local_table = TableInfo(child_list,column_list,'mlu_local',3)
    all_tables.append(local_table)
    
    district_relationship = TableKeyRelationship(local_table,'district_id')
    child_list = []
    column_list = []
    child_list.append(district_relationship)
    column_list.append('district_id')
    column_list.append('district_name')
    district_table = TableInfo(child_list,column_list,'mlu_district',2)
    all_tables.append(district_table)
    
    city_relationship = TableKeyRelationship(district_table,'city_id')
    child_list = []
    column_list = []
    child_list.append(city_relationship)
    column_list.append('city_id')
    column_list.append('city_name')
    city_table = TableInfo(child_list,column_list,'mlu_city',1)
    all_tables.append(city_table)

    return all_tables

# find best table to match input attribute
def _find_table(attr_str,all_tables):
    for table in all_tables:
        if attr_str == table.name:
            return table
    return None

def _find_join_path_table_name(table1_name,table2_name,str_path,all_tables):
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
    #print str_path, table1.name, table2.name, flag1, flag2
    if flag1 == True and flag2 == True:
        result = _find_join_path(table1,table2,str_path)
    return result
    
def _find_join_path(table1,table2,str_path):
    # table1 already added in join tree
    flag1 = str_path.find(table1.name) != -1
    flag2 = str_path.find(table2.name) != -1    
    if flag1 & flag2:
        return str_path

    # table1 is equal to table2
    if table1 == table2:
        return str_path
    # table1 doesn't have child and it's not in join tree
    if len(table1.child_list) == 0:
        return -1
    
    for sub_table_rel in table1.child_list:
        # str_path doesn't have this table
        t_index = str_path.find(sub_table_rel.rel_table.name)
        if t_index == -1:
            str_path = str_path + " join " + sub_table_rel.rel_table.name + " a" + str(sub_table_rel.rel_table.index) + " on " + "a" + str(table1.index) + "." + sub_table_rel.key_column + " = " + " a" + str(sub_table_rel.rel_table.index) + "." + table1.name + "_" + sub_table_rel.key_column
            result_mid = _find_join_path(sub_table_rel.rel_table,table2,str_path)
            if result_mid != -1:
                return result_mid
        else:
            return str_path + " join " + table1.name + " a" + str(table1.index) + " on " + "a" + str(table1.index) + "." + sub_table_rel.key_column + " = " + " a" + str(sub_table_rel.rel_table.index) + "." + table1.name + "_" + sub_table_rel.key_column
    return -1