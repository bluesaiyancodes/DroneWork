import cv2
import numpy as np
from djitellopy import Tello
import time

tello = Tello()

tello.connect()

print("Battery: ", tello.get_battery())

tello.streamon()
tello.takeoff()
# Initial Takeoff
tello.send_rc_control(0, 0, 20, 0)
#Let the drone settle
time.sleep(3)
#Set Range (distance from drone) in terms of area of bounding box
fbRange = [6200, 6800]
pid = [0.4, 0.4, 0]
# Width height of video frame
w, h = 360, 240
# previous error (previous difference between center of bounding box and center or video frame)
pError = 0

# function to find face in a frame
def faceDetection(img, w, h):

    faceCascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
    img = cv2.resize(img, (w, h))

    imgGray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    faces = faceCascade.detectMultiScale(imgGray, 1.2, 8)

    centerBoundBox = []

    areaBoundBox = []

    for (x, y, w, h) in faces:

        cv2.rectangle(img, (x, y), (x + w, y + h), (0, 0, 255), 2)

        cx = x + w // 2

        cy = y + h // 2

        area = w * h

        cv2.circle(img, (cx, cy), 5, (0, 255, 0), cv2.FILLED)

        centerBoundBox.append([cx, cy])

        areaBoundBox.append(area)

    if len(areaBoundBox) != 0:

        i = areaBoundBox.index(max(areaBoundBox))

        return img, [centerBoundBox[i], areaBoundBox[i]]

    else:

        return img, [[0, 0], 0]


def faceTracking( info, w, pid, pError):

    area = info[1]

    x, y = info[0]

    fb = 0

    error = x - w // 2
    #error = (x//1.7) - (w // 2)
    speed = pid[0] * error + pid[1] * (error - pError)

    speed = int(np.clip(speed, -100, 100))

    if area > fbRange[0] and area < fbRange[1]:

        fb = 0

    elif area > fbRange[1]:

        fb = -20

    elif area < fbRange[0] and area != 0:

        fb = 20

    if x == 0:

        speed = 0

        error = 0

    #print(speed, fb)

    tello.send_rc_control(0, fb, 0, speed)

    return error                                        



# Record Video
# Define the codec and create VideoWriter object
fourcc = cv2.VideoWriter_fourcc(*"XVID")
out = cv2.VideoWriter('output1.avi', fourcc, 10, (w, h))




while True:
    frame = tello.get_frame_read().frame

    img, info = faceDetection(frame, w, h)

    pError = faceTracking( info, w, pid, pError)

    cv2.imshow('Tello', img)
    # output the frame
    out.write(img)

    
    #print(info)
    #print("Center", info[0], "Area", info[1])

    #cv2.waitKey(1)

    # Stop if escape key is pressed
    k = cv2.waitKey(30) & 0xff
    if k==27:
        tello.land()
        tello.streamoff()
        out.release()
        break
        
'''
    if cv2.waitKey(1) & 0xFF == ord('q'):

        tello.land()
        tello.streamoff()
        out.release()


        break
'''