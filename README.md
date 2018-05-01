# LiveboxCam
Android app for Livebox project
# MainActivity
- Main user Interface
- Runs all logic and services
- Periodically requests image classification result of the scene
# Entities - custom classes folder
- Beacon class extends BluetoothDevice and has last seen time and is sleeping
# graphics - all OpenGL related classes
- CustomGLSurface - surface view for visual augmentation
- CustomRenderer - rendering entity
- RectangleFrame and Triangle - custom shapes
# Services - all communication related services
- BLECollector - singleton class which keeps track of all scaned BLE devices
- BluetoothDiscoveryService - periodic BLE scanning, collects data to BLECollector
- ConnectionAsyncTask - Requests for data of Beacon Discovered or interacted
- ImageSendAsyncTask - image classification requests, respond with livebox owner
- MediaService - Request for media stream object, such as video or audio
# Utils
- ImageUtils - image conversion and serialization class
