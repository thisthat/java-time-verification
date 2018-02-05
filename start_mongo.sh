# mongo --eval "db.stats()"  # do a simple harmless command of some sort

RESULT=$(mongo --eval "db.stats()")   # returns 0 if mongo eval succeeds

if [ $RESULT -ne 0 ]; then
    screen -d -m bash mongod
    exit 1
fi
