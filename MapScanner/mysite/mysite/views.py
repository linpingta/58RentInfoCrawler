#-*- coding:utf-8 -*-

from django.shortcuts import get_object_or_404, render_to_response, redirect
from django.http import Http404, HttpRequest, HttpResponse

import MySQLdb

def index(request):
    return HttpResponse('Hello Map')

def map(request, map_id):
    try:
        conn = _connect_sql()
        cur = conn.cursor()
        
        result = _select(cur,map_id)
        hyperlink = _get_link(result)
        
        cur.close()
        conn.close()
    except MySQLdb.Error, e:
        print 'MySQL Execution Error'
        
    return HttpResponse('Hello ' + map_id)

def _connect_sql():
    return MySQLdb.connect(host='localhost', user='root', passwd='feixuluohua', db='rent_info', port=3306, charset='gb2312')

def _select(cur,map_id):
    cur.execute('select * from mlu_city')
    #result = cur.fetchmany(5)
    result = cur.fetchall()
    
    return result

def _get_link(result):
    for row in result:
        print row[1]