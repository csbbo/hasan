import datetime
import time
import subprocess
from apscheduler.schedulers.blocking import BlockingScheduler as Scheduler

def work1():
    now = datetime.datetime.now()
    now_time = now.strftime('%Y-%m-%d %H:%M:%S')
    data = subprocess.check_output('curl "localhost:3000/user?scheduler=now"', shell=True)
    print(data.decode('utf-8'))

def work2():
    now = datetime.datetime.now()
    now_time = now.strftime('%Y-%m-%d %H:%M:%S')
    print(now_time)
    time.sleep(2)

def work3():
    now = datetime.datetime.now()
    now_time = now.strftime('%Y-%m-%d %H:%M:%S')
    print("每天定时任务")

def job():
    scheduler = Scheduler()
    scheduler.add_job(work1, 'interval', seconds=2)
    scheduler.add_job(work2, 'interval', seconds=3)
    scheduler.add_job(work3, 'cron', day_of_week='0-6', hour=11, minute=9, second=0)
    scheduler.start()

if __name__ == '__main__':
    job()