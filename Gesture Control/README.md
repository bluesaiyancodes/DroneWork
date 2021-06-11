# DJI Tello Hand Gesture control


## Setup
### 1. Installing pip packages
First, we need to install python dependencies. Make sure you that you are using `python3.7`

List of packages
```sh
ConfigArgParse == 1.2.3
djitellopy == 1.5
numpy == 1.19.3
opencv_python == 4.5.1.48
tensorflow == 2.5.0rc0
mediapipe == 0.8.3
```

Install
```sh
pip3 install -r requirements.txt
```
### 2. Connect Tello
Turn on drone and connect computer to its WiFi

Next, run the following code to verify connectivity

```sh
python3 tests/connection_test.py
```

On successful connection

```json
1. Connection test:
Send command: command
Response: b'ok'


2. Video stream test:
Send command: streamon
Response: b'ok'
```

If you get such output, you may need to check your connection with the drone

```json
1. Connection test:
Send command: command
Timeout exceed on command command
Command command was unsuccessful. Message: False


2. Video stream test:
Send command: streamon
Timeout exceed on command streamon
Command streamon was unsuccessful. Message: False
```

## Usage
The most interesting part is demo. There are 2 types of control: keyboard and gesture. You can change between control types during the flight. Below is a complete description of both types.

Run the following command to start the tello control :

```sh
python3 main.py
```

### Keyboard control
To control the drone with your keyboard at any time - press the `k` key.

The following is a list of keys and action description -

* `k` -> Toggle Keyboard controls
* `g` -> Toggle Gesture controls
* `Space` -> Take off drone(if landed) **OR** Land drone(if in flight)
* `w` -> Move forward
* `s` -> Move back
* `a` -> Move left
* `d` -> Move right
* `e` -> Rotate clockwise
* `q` -> Rotate counter-clockwise
* `r` -> Move up
* `f` -> Move down
* `Esc` -> End program and land the drone 


### Gesture control 

By pressing `g` you activate gesture control mode. 

## Adding new gestures
Hand recognition detector can add and change training data to retrain the model on the own gestures. But before this,
there are technical details of the detector to understand how it works and how it can be improved
### Technical details of gesture detector
Mediapipe Hand keypoints recognition is returning 3D coordinated of 20 hand landmarks. For our
model we will use only 2D coordinates.


Then, these points are preprocessed for training the model in the following way.

After that, we can use data to train our model. Keypoint classifier is a simple Neural network with such 
structure

### Creating dataset with new gestures
First, pull datasets from Git LFS. [Here](https://github.com/git-lfs/git-lfs/wiki/Installation) is the instruction of how 
to install LFS. Then, run the command to pull default csv files
```sh
git lfs install
git lfs pull
```

After that, run `main.py` and press "n" to enter the mode to save key points
(displayed as **MODE:Logging Key Point**）


If you press "0" to "9", the key points will be added to [model/keypoint_classifier/keypoint.csv](model/keypoint_classifier/keypoint.csv) as shown below.<br>
1st column: Pressed number (class ID), 2nd and subsequent columns: Keypoint coordinates

In the initial state, 7 types of learning data are included as was shown [here](#Gesture-control). If necessary, add 3 or later, or delete the existing data of csv to prepare the training data.
### Notebook for retraining model

Open [Keypoint_model_training.ipynb](Keypoint_model_training.ipynb) in Jupyter Notebook.
Change the number of training data classes,the value of **NUM_CLASSES = 3**, and path to the dataset. Then, execute all cells
and download `.tflite` model

Do not forget to modify or add labels in `"model/keypoint_classifier/keypoint_classifier_label.csv"`

#### Grid Search
❗️ Important ❗️ The last part of the notebook is an experimental part of the notebook which main functionality is to test hyperparameters of the model structure. In a nutshell: grid search using TensorBoard visualization. Feel free to use it for your experiments.


## Repository structure
<pre>
│  main.py
│  Keypoint_model_training.ipynb
│  config.txt
│  requirements.txt
│  
├─model
│  └─keypoint_classifier
│      │  keypoint.csv
│      │  keypoint_classifier.hdf5
│      │  keypoint_classifier.py
│      │  keypoint_classifier.tflite
│      └─ keypoint_classifier_label.csv
│ 
├─gestures
│   │  gesture_recognition.py
│   │  tello_gesture_controller.py
│   └─ tello_keyboard_controller.py
│          
├─tests
│   └─connection_test.py
│ 
└─utils
    └─cvfpscalc.py
</pre>
### app.py
Main app which controls the functionality of drone control and gesture recognition<br>
App also includes mode to collect training data for adding new gestures.<br>

### keypoint_classification.ipynb
This is a model training script for hand sign recognition.

### model/keypoint_classifier
This directory stores files related to gesture recognition.<br>

* Training data(keypoint.csv)
* Trained model(keypoint_classifier.tflite)
* Label data(keypoint_classifier_label.csv)
* Inference module(keypoint_classifier.py)

### gestures/
This directory stores files related to drone controllers and gesture modules.<br>

* Keyboard controller (tello_keyboard_controller.py)
* Gesture controller(tello_keyboard_controller.py)
* Gesture recognition module(keypoint_classifier_label.csv)

### utils/cvfpscalc.py
Module for FPS measurement.



# Reference
* [MediaPipe](https://github.com/google/mediapipe)
* [MediaPipe Hand gesture recognition (by Kazuhito00)](https://github.com/Kazuhito00/hand-gesture-recognition-using-mediapipe)
* [Tello SDK python interface](https://github.com/damiafuentes/DJITelloPy)

