
OPTIONS= -v # add -v to set verbose mode, -a to print all consumptions

#PORT=`IFS=$\'\\n\';ls /dev/ttyUSB* -l | head -n 1 | sed 's/.*:[0-9]\\{2\\} //'`
PORT=/dev/ttyUSB0
#PORT=/dev/ttyUSB1

#MACADRESS="000D6F000098A5B5"
#MACADRESS="000D6F000076A98E"
MACADRESS="000D6F000072AB5E"

#plugwise_MAC=000D6F000076C10A
#plugwise_MAC=000D6F000076AE46

all:
	@echo "sending command via port " $(PORT) " to adress " $(MACADRESS)
	@echo on  : python pol.py -p $(PORT) -o $(MACADRESS) $(OPTIONS)
	@echo off : python pol.py -p $(PORT) -f $(MACADRESS) $(OPTIONS)
	@echo watt: python pol.py -p $(PORT) -w $(MACADRESS) $(OPTIONS)

on:
	@python pol.py -p $(PORT) -o $(MACADRESS) $(OPTIONS)

off:
	@python pol.py -p $(PORT) -f $(MACADRESS) $(OPTIONS)

watt:
	@python pol.py -p $(PORT) --watt $(MACADRESS) $(OPTIONS)
	
test:
	@python pol.py -p $(PORT) --test $(MACADRESS)

follow:
	@while true ; do echo; echo; date; python pol.py -p $(PORT) -w $(MACADRESS) -a $(OPTIONS); sleep 2; done
	
overload:
	@while true ; do echo; echo; date; if python pol.py -p $(PORT) -w $(MACADRESS) -a $(OPTIONS) ; then echo; else exit; fi; done
