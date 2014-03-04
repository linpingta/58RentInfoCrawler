# -*- coding: gb2312 -*-
__author__ = 'linpingta'

from LvStationInfo import LvStationInfo

import pickle
import urllib
import math
import simplejson as json

class LvSubwayApp:
    station_list = []

    def __init__(self):
        self.station_list = []

    def init_station_geocode(self):
        # init station list
        line_num_list = [1, 2, 4, 10, 13]
        name_list = [['ƻ��԰', '�ų�·', '�˽�����԰', '�˱�ɽ', '��Ȫ·', '�����', '����·', '������', '���²����', 'ľ�ص�', \
                      '����ʿ·', '������', '����', '�찲����', '�찲�Ŷ�', '������', '����', '������', '������', '��ó', '����·', '�Ļ�', '�Ļݶ�'],
                     ['��ֱ��', '����ʮ��', '������', '������', '����վ', '������', 'ǰ��', '��ƽ��', '������', '������', '������', \
                      '������', '����ׯ', '��ֱ��', '��ˮ̶', '��¥���', '������', 'Ӻ�͹�'],
                     ['�����ű�', '������', '��Է', 'Բ��԰', '�йش�', '�����ׯ', '�����ѧ', 'κ����', '����ͼ���', '��ֱ��', \
                      '�½ֿ�', 'ƽ����', '����', '�龳��ͬ', '����', '������', '���п�', '��Ȼͤ', '������վ', '��ұ�', '������', '��������', \
                      '�¹�', '������', '���׵걱', '���׵���', '��԰', '��Դ·', '�ƴ������', '�ƴ��վ', '���ׯ', '�칬Ժ'],
                     ['����', '������', '������', '������', '������̨', '������', '������', '����Ӫ', '�͹�', '���ݽ�', '�����ׯ', \
                      '֪����', '֪��·', '������', 'ĵ��԰', '������', '������', '������', '���������Ͽ�', '��ҩ��', '̫����', '��Ԫ��', '������', \
                      'ũҵչ����', '�Ž��', '����¥', '��̨Ϧ��', '��ó', '˫��', '����', '�˼�԰', 'ʮ���', '������', '������', \
                      '�μ�ׯ', 'ʯ��ׯ', '�����', '������', '�ͼ���'],
                     ['������', '֪��·', '�����', '�ϵ�', '������', '����', '������', '��Ӫ', '��ˮ��', '��Է', '������', '��ҩ��', '������', '����',
                      '��ֱ��', '��ֱ��']
        ]

        k = 0
        for line_num in line_num_list:
            for name in name_list[k]:
                #print name
                name = name + '����վ'
                hyperlink = 'http://api.map.baidu.com/geocoder/v2/?address=' + name + '&output=json&ak=OspflLhgVDoR5P57YP6hlBV7&city=����'
                #print hyperlink
                geo_info = urllib.urlopen(hyperlink).read()
                geo_info_array = json.loads(geo_info)
                #print str(geo_info_array['result'])
                if geo_info_array['result'] == '[]':
                    continue
                tmp_latitude = geo_info_array['result']['location']['lat']
                tmp_longitude = geo_info_array['result']['location']['lng']
                #print tmp_latitude, tmp_longitude
                s = LvStationInfo(name, line_num, tmp_latitude, tmp_longitude)
                self.station_list.append(s)
            k = k + 1

        # write result in file
        fp = open('init_station_geocode.txt', 'w')
        pickle.dump(self.station_list, fp, 0)
        fp.close()

        # test reload station info
        '''
        print 'reload value'
        fp = open('init_station_geocode.txt','r')
        station_reload_list = pickle.load(fp)
        for element in station_reload_list:
            print element
        fp.close()
        '''

    def get_nearest_subway_station(self,detail_local_name):
        # reload subway station info
        fp = open('init_station_geocode.txt', 'r')
        station_reload_list = pickle.load(fp)

        # get user input
        hyperlink = 'http://api.map.baidu.com/geocoder/v2/?address=' + detail_local_name + '&output=json&ak=OspflLhgVDoR5P57YP6hlBV7&city=����'
        detail_local_info = urllib.urlopen(hyperlink).read()
        detail_local_info_array = json.loads(detail_local_info)

        try:
            detail_local_name_latitude = detail_local_info_array['result']['location']['lat']
            detail_local_name_longitude = detail_local_info_array['result']['location']['lng']
        except:
            print u'���������ַ�޷����������Ժ�'
            return

        # evaluate distance
        # evaluate every line
        line_num_list = [1, 2, 4, 10, 13]
        min_distance_dict = {}
        for line_num in line_num_list:
            min_distance = 1000.0
            for station in station_reload_list:
                '''
                distance = math.fabs(detail_local_name_latitude - station.latitude) + math.fabs(detail_local_name_longitude - station.longitude)
                if distance < min_distance:
                    min_distance = distance
                    min_station_info = station
                '''
                if station.line_num == line_num:
                    part_distance = math.fabs(detail_local_name_latitude - station.latitude) + math.fabs(
                        detail_local_name_longitude - station.longitude)
                    if part_distance < min_distance:
                        min_distance = part_distance
                        min_distance_dict[line_num] = [station, min_distance]

        # evaluate the minest of all
        min_distance = 1000.0
        min_key = -1
        min_station_every_line = []
        for key, value in min_distance_dict.iteritems():
            print key, value[0].name, value[1]
            min_station_every_line.append(value[0])
            if value[1] < min_distance:
                min_distance = value[1]
                min_key = key
                min_station_info = value[0]

        # calculate accurate distance
        hyperlink = 'http://api.map.baidu.com/direction/v1/routematrix?output=json&origins=' + detail_local_name + '&destinations=' + min_station_info.name + '&ak=OspflLhgVDoR5P57YP6hlBV7'
        data_back = urllib.urlopen(hyperlink).read()
        json_array = json.loads(data_back)

        min_accurate_distance = json_array['result']['elements'][0]['distance']['value']
        print min_station_info.name, min_distance, min_accurate_distance

        return [min_station_info.name, min_distance, min_accurate_distance, min_station_every_line]