import sys, optparse
from proton import *

parser = optparse.OptionParser(usage="usage: %prog [options] <addr_1> ... <addr_n>",
                               description="simple message receiver")
parser.add_option("-c", "--certificate", help="path to certificate file")
parser.add_option("-k", "--private-key", help="path to private key file")
parser.add_option("-p", "--password", help="password for private key file")

opts, args = parser.parse_args()

targetadd = "amqp://localhost/test-queue.abc"

mng = Messenger()
mng.certificate=opts.certificate
mng.private_key=opts.private_key
mng.password=opts.password
mng.start()


msg = Message()
msg.address = targetadd
msg.body = "pyhahaha"
mng.put(msg)
mng.send()
print "send message"

mng.stop()
