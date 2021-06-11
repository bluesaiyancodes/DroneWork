import cv2
import numpy as np


fbRange = [6200, 6800]
pid = [0.4, 0.4, 0]
w, h = 640, 480
pError = 0
# Load the cascade
face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

# To capture video from webcam. 
cap = cv2.VideoCapture(0)
# To use a video file as input 
# cap = cv2.VideoCapture('filename.mp4')

def findFace(img, w, h):

    faceCascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

    img = cv2.resize(img, (w, h))

    imgGray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    faces = faceCascade.detectMultiScale(imgGray, 1.2, 8)

    myFaceListC = []

    myFaceListArea = []

    for (x, y, w, h) in faces:

        cv2.rectangle(img, (x, y), (x + w, y + h), (0, 0, 255), 2)

        cx = x + w // 2

        cy = y + h // 2

        area = w * h

        cv2.circle(img, (cx, cy), 5, (0, 255, 0), cv2.FILLED)

        myFaceListC.append([cx, cy])

        myFaceListArea.append(area)

    if len(myFaceListArea) != 0:

        i = myFaceListArea.index(max(myFaceListArea))

        return img, [myFaceListC[i], myFaceListArea[i]]

    else:

        return img, [[0, 0], 0]

def trackFace( info, w, pid, pError):

    area = info[1]

    x = info[0][0]

    fb = 0

    error = (x//1.7) - (w // 2)
    print(error)

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

    print(speed, fb)

    #tello.send_rc_control(0, fb//2, 0, -speed//2)

    return error         

# Define the codec and create VideoWriter object
fourcc = cv2.VideoWriter_fourcc(*"XVID")
out = cv2.VideoWriter('output.avi', fourcc, 10, (w, h))


while True:
    # Read the frame
    _, img = cap.read()
    # Convert to grayscale
    #gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Detect the faces
    #faces = face_cascade.detectMultiScale(gray, 1.1, 4)
    # Draw the rectangle around each face
    #for (x, y, w, h) in faces:
        #cv2.rectangle(img, (x, y), (x+w, y+h), (255, 0, 0), 2)
    # Display
    

    img, info = findFace(img, w, h)

    pError = trackFace( info, w, pid, pError)

    out.write(img)
    
    cv2.imshow('img', img)
    


    # Stop if escape key is pressed
    k = cv2.waitKey(30) & 0xff
    if k==27:
        break
# Release the VideoCapture object
cap.release()
out.release()
cv2.destroyAllWindows()