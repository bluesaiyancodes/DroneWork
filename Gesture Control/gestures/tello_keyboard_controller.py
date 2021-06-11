from djitellopy import Tello

class TelloKeyboardController:
    def __init__(self, tello: Tello):
        self.tello = tello

    def control(self, key):
        if key == ord('w'):
            self.tello.move_forward(20)
        elif key == ord('s'):
            self.tello.move_back(20)
        elif key == ord('a'):
            self.tello.move_left(20)
        elif key == ord('d'):
            self.tello.move_right(20)
        elif key == ord('e'):
            self.tello.rotate_clockwise(20)
        elif key == ord('q'):
            self.tello.rotate_counter_clockwise(20)
        elif key == ord('r'):
            self.tello.move_up(20)
        elif key == ord('f'):
            self.tello.move_down(20)



