import threading
import socket
import cv2


print("\nTello Video Stream Program\n")


class Tello:
    def __init__(self):
        self._running = True

    def terminate(self):
        self._running = False
        self.video.release()
        cv2.destroyAllWindows()

    def recv(self):
        """ Handler for Tello states message """
        while self._running:
            try:
                ret, frame = cv2.VideoCapture("udp://@0.0.0.0:11111").read()
                if ret:
                    # Resize frame
                    height, width, _ = frame.shape
                    new_h = int(height / 2)
                    new_w = int(width / 2)

                    # Resize for improved performance
                    new_frame = cv2.resize(frame, (320, 240))

                    # Load the cascade
                    face_cascade = cv2.CascadeClassifier('resources/haarcascade_frontalface_default.xml')
                    # Convert to grayscale
                    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
                    # Detect the faces
                    faces = face_cascade.detectMultiScale(gray, 1.1, 4)
                    # Draw the rectangle around each face
                    for (x, y, w, h) in faces:
                        cv2.rectangle(frame, (x, y), (x+w, y+h), (255, 0, 0), 2)

                    # Display the resulting frame
                    cv2.imshow('Tello', new_frame)
                # Wait for display image frame
                # cv2.waitKey(1) & 0xFF == ord('q'):
                cv2.waitKey(1)
            except Exception as err:
                print(err)


""" Start new thread for receive Tello response message """
t = Tello()
recvThread = threading.Thread(target=t.recv)
recvThread.start()

while True:
    try:
        # Get input from CLI
        msg = input()

        # Check for "end"
        if msg == "end":
            t.terminate()
            recvThread.join()
            print("\nGood Bye\n")
            break
    except KeyboardInterrupt:
        t.terminate()
        recvThread.join()
        break