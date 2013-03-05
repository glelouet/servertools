from CrcMoose import *
import sys
sys.path.append("/usr/lib/python2.6/dist-packages/")
try:
	import serial
except ImportError:
	print "cannot load the serial module. Check that it is installed, and in the python path"
	exit(1)
import getopt, os, traceback
import struct, math
from serial.serialutil import SerialException

class Plugwise:
    
    
    W_S_PER_PULSE = 2.116 #the number of W.s a pulse represents
    PLUGWISE_BASE_CONSO=0
    SERIAL_RTIMEOUT=1.5
    ERROR_RETURN=(-1,-1,-1,-1,-1,-1)
    verbose=0
				
				
    HEADER = '\x05\x05\x03\x03'
    ENDLINE = '\x0d\x0a'
    CALIBRATIONCODE = '0026'
    POWERCHANGECODE = '0017'
    POWERINFOCODE = '0012'
    CALIBRATIONRESPONSECODE = '0027'
    POWERINFORESPONSECODE = '0013'
    
    
    class Frame:
        
        MACADRESS_SIZE=16
        FRAME_FIFOUNICAST_STARTUP = "PutFifoUnicast"
        
        complete = 0
        fullFrame=''# frame actually read since startup beginning. full data read is discarded+fullframe
        discarded=''# data that have been read from the start of the frame to just before the startup token
        startup=''#the token accepted as the startup of the frame
        frametype=-1
        handle=''
        macDest=''
        clusterId=''
        status=''
        responseCode=''
        buffer=''
        
        def __init__(self, serial):
            try :
                self.setSerial(serial)
                while not self.isStartupToken(self.startup) :
                    if(len(self.readOneChar())<1):
                        raise Exception("cannot open data from the serial")
                    if(self.startupBeginning(self.buffer)):
                        self.startup=self.buffer
                        self.discardRead()
                    else :
                        self.discardBuffer()
                self.frametype=int(self.nextToken())
                self.handle = int(self.tokenAfter("Handle"))
                self.nextToken();
                self.macDest=self.getNextChars(self.MACADRESS_SIZE)
                self.getNextChars(25)
                
                if(len(self.getNextChars(1))<1) :
                    print >> sys.stderr, "error : no more data for header, discarding"
                    raise Exception()
                self.clusterId=self.tokenAfter("ClusterId")
                self.status=self.tokenAfter("Status")
                if(len(self.fullFrame)>200):
                    self.eatAll()
                    print >> sys.stderr, "error :  frame header too long, discarding"
                    raise Exception()
                self.readTillChar(":")
                self.getNextChars(1+self.MACADRESS_SIZE+6)
                self.responseCode=self.getNextChars(4)
                self.complete=1
                #print self.debug()
            except (Exception, KeyboardInterrupt, SystemExit) as e:
                self.eatAll()
                traceback.print_exc()
                print self.debug()
                raise e
            
        def setSerial(self, serial):
            self.serial = serial
            
        def discardRead(self):
            ''' put previously handled data from fulframe to the as discarded, leaving the buffer '''
            self.discarded+=self.fullFrame
            self.fullFrame=''
        
        def isStartupToken(self,token):
            ''' return true if the token is a startup token, that is the beginning of a frame'''
            ret = (token==Plugwise.Frame.FRAME_FIFOUNICAST_STARTUP)
            return ret
        
        def startupBeginning(self, token):
            ''' return true if the token is the beginning of a startup one'''
            if(len(token)<1): return 0
            return Plugwise.Frame.FRAME_FIFOUNICAST_STARTUP.startswith(token)
        
        def getNextChars(self, nb):
            ''' return the next at least nb chars on the serial.
            If there is not nb chars to read, return the next remaining chars on the serial'''
            self.flushBuffer()
            while len(self.buffer)<nb :
                read=self.readOneChar()
                if(len(read)<1):
                    print >> sys.stderr, "not enough chars in nextChars",nb
                    return self.flushBuffer()
            return self.flushBuffer()
        
        def readTillChar(self, endChar):
            ''' read the serial until that char is read, or EOF. return true if that char was found, false elsewhere
            '''
            self.flushBuffer()
            while(1):
                char = self.readOneChar()
                if(len(char)<1):
                    return 0
                if(char.startswith(endChar)):
                    return 1
                
        
        def tokenAfter(self, suffix):
            ''' read the serial tokens until one is found, that ends with suffix. then return the next token found
             that ends with whitespace or EOF
             returns '' if there is not enough data on the serial
            ''' 
            lastchar=''
            while not lastchar.endswith(suffix) :
                lastchar = self.nextToken()
                if(len(lastchar)==0):
                    return ''
            return self.nextToken()
        
        def nextToken(self):
            '''
            read the entry until a whitespace is read, removes the whitespaces,
            append the read token to the buffer and return the token
            If there is not enough data on the buffer, returns ''
            '''
            self.flushBuffer()
            char = self.readOneChar()
            while(char==' ' and len(char)>0):
                char = self.readOneChar()
            while(char!= ' ') :
                char=self.readOneChar()
#                print "got", char, "char from serial"
                if(len(char)<1):
                    return ''
#            print " next token", self.buffer
            return self.flushBuffer()[:-1]
        
        def readOneChar(self):
            '''
            read the next char in the serial, append it to the buffer and return the char
            '''
            char = self.serial.read()
            self.buffer+=char
            return char
        
        def eatAll(self):
            '''read all available chars until there is no more in the serial.
            remaining data is put in the buffer'''
            char=' '
            while(len(char)>0):
                char=self.readOneChar()
                
        
        def flushBuffer(self):
            '''
            put all data in the buffer as "handled", then empty the buffer and return the old value of the buffer
            '''
            self.fullFrame+= self.buffer
            ret= self.buffer
            self.discardBuffer()
            return ret
            
        def discardBuffer(self):
            '''
            empty the buffer
            '''
            self.buffer=''
        
        def debug(self):
            serial = self.serial
            del self.serial
            ret = str(self.__dict__).replace(", ", ", \n\t")
            self.serial=serial
            return ret
           
    
    class FrameLine:
      '''
     decoded line from the serial input
      '''
      
      line=''
      body=''
      command=''
      localCRC='none'
      sequence=0
      
      def __init__(self, read):
        self.line=read
        if(read.startswith(Plugwise.HEADER)):
          self.body=read[4:-6]
          self.crc=read[-6:-2]
          self.localCRC=Plugwise.GetCRC16(self.body)
          self.command=self.body[:4]
          self.sequence=Plugwise.hexToInt(self.body[4:8])
      def __str__(self):
        return "frameline : <"+Plugwise.escapeASCII(self.line)+"> command:<"+self.command+">sequence:<"+"%i"%self.sequence+">"


    @staticmethod
    def bytesToHexString(str):
        ret = ""
        for char in str :
            ret+=" %02x"%ord(char)
        return ret
       
    @staticmethod
    def escapeASCII(str):
    	ret=""
    	for char in str:
    		val = ord(char)
    		if(val>=ord('a') and val<=ord('z')
					or val>=ord('A') and val<=ord('Z')
					or val>=ord('0') and val<=ord('9')):
    			ret+=char
    		else:
    			ret+="\\%02x "%val
    	return ret

    @staticmethod
    def pulseToKWH(pulses):
        output = pulses * Plugwise.W_S_PER_PULSE /3600000;
        return output

    @staticmethod
    def pulseToWatt(pulses, secs):
        """
        convert a number of pulses on a given number of sec to a power, in W
        """
        result = pulses * Plugwise.W_S_PER_PULSE / secs -Plugwise.PLUGWISE_BASE_CONSO
        return result
    @staticmethod
    def GetCRC16(value):
        value = CRC16X.calcString(value)
        format = ("%%0%dX" % ((CRC16X.width + 3) // 4))
        return format % value

    @staticmethod
    def hexToFloat(hexstr):
        intval = int(hexstr, 16)
        bits = struct.pack('L', intval)[:4]
        flaot = struct.unpack('f', bits)[0]
        return flaot

    @staticmethod
    def hexToInt(hexstr):
        return int(hexstr, 16)
     	
    @staticmethod
    def decodeFrame(read):
    	return Plugwise.FrameLine(read)
       
       		
    def __init__(self, port, macaddress):
				self.serial = serial.Serial(port, "115200")
				self.serial.setTimeout(self.SERIAL_RTIMEOUT)
				
				self.macaddress = macaddress
				self.gain_a = 0
				self.gain_b = 0
				self.offtot = 0
				self.offruis = 0
				self.correctedpulses = 0


    def GetCalibrationInfo(self):
        self.SendCommand(self.CALIBRATIONCODE +  self.macaddress)
        result = self.GetResult(self.CALIBRATIONRESPONSECODE)
        if(len(result)<1) :
            print "bad calibration frame"
            return 0
        if(self.verbose) : print "  calibration :", result[:-1]# -1 to remove the last \n
        self.gain_a = self.hexToFloat(result[0:8])
        self.gain_b = self.hexToFloat(result[8:16])
        self.offtot = self.hexToFloat(result[16:24])
        self.offruis = self.hexToFloat(result[24:32])
#        print "  gain_a:",self.gain_a, ", gain_b:",self.gain_b, ", offtot:",self.offtot, ", offruis:",self.offruis
        return 1
        
    def PulseCorrection(self, pulses):
        if(pulses<=0): return 0
        value = pulses / 1.0;
        out = 1.0 * (((pow(value + self.offruis, 2.0) * self.gain_b) + ((value + self.offruis) * self.gain_a)) + self.offtot)
        if(self.verbose) : print " correction %f in %f"%(pulses, out)
        return out
    
    def strToPulses(self, str):
        if(str == "FFFF") : return 0
        else : return self.PulseCorrection(Plugwise.hexToInt(str))

    def GetPowerInfo(self):
        if ( not self.GetCalibrationInfo() ):
            return Plugwise.ERROR_RETURN
        self.SendCommand(self.POWERINFOCODE + self.macaddress)
        result = self.GetResult(self.POWERINFORESPONSECODE)
        if(len(result)<1):
            return Plugwise.ERROR_RETURN
        if(self.verbose): 
            print "  power :",
            print result[:-1]
        onesecpulses = self.strToPulses(result[:4])
        eightsecpulses = self.strToPulses(result[4:8])
        allpulses = self.PulseCorrection(Plugwise.hexToInt(result[8:16]))
        return self.pulseToWatt(onesecpulses,1),\
                self.pulseToWatt(eightsecpulses, 8),\
                self.pulseToKWH(allpulses),\
                onesecpulses,\
                eightsecpulses,\
                allpulses

    def SetPowerState(self, newstate):
        self.SendCommand(self.POWERCHANGECODE + self.macaddress + newstate)

    def SendCommand(self, command):
        self.write(self.HEADER + command + Plugwise.GetCRC16(command) + \
        self.ENDLINE )

    def GetResult(self, responsecode):
        frameCharSize = 0
        
        if responsecode == self.CALIBRATIONRESPONSECODE:
            frameCharSize = 58
        elif responsecode == self.POWERINFORESPONSECODE:
            frameCharSize = 54
        elif responsecode == "0000":
            frameCharSize = 0
            
        head = Plugwise.Frame(self.serial)
        if head.complete and head.responseCode==responsecode:
            try :
                data = self.serial.read(frameCharSize)
                if(self.verbose) :
                    print >>sys.stderr,"  head size %d, data size %d"%(len(head.fullFrame), len(data))
                    print >>sys.stderr,"  data received:",(data[:-1])
                return data[20:]
            except ValueError as error:
                print >>sys.stderr,"  received head : ", head
                print >>sys.stderr,"  awaiting %d chars"%self.serial.inWaiting()
                raise error
        else :
            print >>sys.stderr, " unexpected frame : complete=",head.complete, "response=",head.responseCode,"expected response=",responsecode\
                , " frame:",head.fullFrame
            return []
           
    def write(self, a):
    	if(self.verbose):
    	  print >>sys.stderr, "writting to serial:",Plugwise.escapeASCII(a)
    	self.serial.write(a)
    
    def readLine(self):
    	read=self.serial.readline()	
    	return read
    
    def nextFrameLine(self):
    	ret = Plugwise.FrameLine(self.readLine())
    	if(self.verbose):
    		print >>sys.stderr, "  frame line read:",ret
    	return ret
           
    def test(self):
    	print "setting power to 0"
    	self.SetPowerState('00')
     	for a in range(0,6):
     		self.nextFrameLine()
    	print "setting power to 1"
     	self.SetPowerState('01')
     	for a in range(0,6):
     		self.nextFrameLine()
    	print "calibration"
    	self.SendCommand(Plugwise.CALIBRATIONCODE+self.macaddress)
    	for a in range(0,6):
     		self.nextFrameLine()
     	exit(0)

def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "p:o:f:w:h:va",
                     ['port=', 'on=', 'off=', 'help', 'watt=', 'test='])
    except getopt.error, why:
        print why
        print_help()

    port = 16
    macaddress = ''
    command = ''
    verbose=0
    all=0

    for o, a in opts:
#        print o,'->',a
        if (o in ('-p', '--port')):
            port = a
        if (o in ('-o', '--on')):
#            newstate = '01'
            macaddress = a
            command = "ON"
        if (o in ('-f', '--off')):
#            newstate = '00'
            macaddress = a
            command = "OFF"
        if (o in ('-w', '--watt')):
            macaddress = a
            command = "POWERINFO"
        if (o in ('-h', '--help')):
            print_help()
            exit(0)
        if (o in ('-v', '--verbose')):
            verbose=1
        if(o== '-a') :
            all=1
        if(o=='--test') :
        		verbose=1
        		macaddress=a
        		command="TEST"
#    print "port:", port, "mac:", macaddress, "command:", command

    if port == '' or macaddress == '':
			print_help()
			exit(1)
    else:
        info=Plugwise.ERROR_RETURN
        try:
            plugwise = Plugwise(port, macaddress)
            plugwise.verbose=verbose
            if command == "ON":
                plugwise.SetPowerState("01")
                exit(0)
            elif command == "OFF":
                plugwise.SetPowerState("00")
                exit(0)
            elif command == "POWERINFO":
                info = plugwise.GetPowerInfo()
                if(all): print info
                else : print info[0]
            elif command == "TEST" :
            		plugwise.test()
            ret =1
            if(info[0]>=0):ret=0
            exit(ret)
        except SerialException :
            print >> sys.stderr, "error : cannot open the usb", port
            if(all) : print (-1,-1,-1)
            else : print -1
            exit(1)
    exit(0)
        
def print_help():
    print
    print "POL (Plugwise on Linux) v0.2 / Maarten Damen"
    print
    print "Usage: pol <options>, return instant consumption"
    print
    print "Options:"
    print "  -p  --port <port>  Serial port of the Plugwise stick, for example: /dev/ttyUSB0"
    print "  -o  --on <macaddress>  Power on plugwise device with specified MAC address"
    print "  -f  --off <macaddress>  Power off plugwise device with specified MAC address"
    print "  -w  --watt <macaddress>  Get current watt usage for device with specified MAC address"
    print "  -h  --help   This help text"
    print "  -v  --verbose to show received trames"
    print "  -a  to print the global consumption on (1s, 8s, total kw.h)"

main()
