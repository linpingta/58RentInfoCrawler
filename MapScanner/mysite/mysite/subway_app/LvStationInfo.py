__author__ = 'linpingta'

class LvStationInfo:
    def __init__(self,name,line_num,latitude=0,longitude=0):
        self.name = name
        self.line_num = line_num
        self.latitude = latitude
        self.longitude = longitude
    def __str__(self):
        return self.name + '_' + str(self.line_num) + '_' + str(self.latitude) + '_' + str(self.longitude)