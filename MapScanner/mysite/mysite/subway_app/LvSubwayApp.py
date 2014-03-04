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
        name_list = [['苹果园', '古城路', '八角游乐园', '八宝山', '玉泉路', '五棵松', '万寿路', '公主坟', '军事博物馆', '木樨地', \
                      '南礼士路', '复兴门', '西单', '天安门西', '天安门东', '王府井', '东单', '建国门', '永安里', '国贸', '大望路', '四惠', '四惠东'],
                     ['东直门', '东四十条', '朝阳门', '建国门', '北京站', '崇文门', '前门', '和平门', '宣武门', '长椿街', '复兴门', \
                      '阜成门', '车公庄', '西直门', '积水潭', '鼓楼大街', '安定门', '雍和宫'],
                     ['安河桥北', '北宫门', '西苑', '圆明园', '中关村', '海淀黄庄', '人民大学', '魏公村', '国家图书馆', '西直门', \
                      '新街口', '平安里', '西四', '灵境胡同', '西单', '宣武门', '菜市口', '陶然亭', '北京南站', '马家堡', '角门西', '公益西桥', \
                      '新宫', '西红门', '高米店北', '高米店南', '枣园', '清源路', '黄村西大街', '黄村火车站', '义和庄', '天宫院'],
                     ['西局', '六里桥', '莲花桥', '公主坟', '西钓鱼台', '慈寿寺', '长春桥', '火器营', '巴沟', '苏州街', '海淀黄庄', \
                      '知春里', '知春路', '西土城', '牡丹园', '健德门', '北土城', '安贞门', '惠新西街南口', '芍药居', '太阳宫', '三元桥', '亮马桥', \
                      '农业展览馆', '团结湖', '呼家楼', '金台夕照', '国贸', '双井', '劲松', '潘家园', '十里河', '分钟寺', '成寿寺', \
                      '宋家庄', '石榴庄', '大红门', '角门西', '纪家庙'],
                     ['大钟寺', '知春路', '五道口', '上地', '西二旗', '龙泽', '回龙观', '霍营', '立水桥', '北苑', '望京西', '芍药居', '光熙门', '柳芳',
                      '东直门', '西直门']
        ]

        k = 0
        for line_num in line_num_list:
            for name in name_list[k]:
                #print name
                name = name + '地铁站'
                hyperlink = 'http://api.map.baidu.com/geocoder/v2/?address=' + name + '&output=json&ak=OspflLhgVDoR5P57YP6hlBV7&city=北京'
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
        hyperlink = 'http://api.map.baidu.com/geocoder/v2/?address=' + detail_local_name + '&output=json&ak=OspflLhgVDoR5P57YP6hlBV7&city=北京'
        detail_local_info = urllib.urlopen(hyperlink).read()
        detail_local_info_array = json.loads(detail_local_info)

        try:
            detail_local_name_latitude = detail_local_info_array['result']['location']['lat']
            detail_local_name_longitude = detail_local_info_array['result']['location']['lng']
        except:
            print u'您的输入地址无法解析，请稍候'
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