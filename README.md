# <p align=center>DrawIt
![img1](https://github.com/toomemag/DrawIt/blob/main/ReadMe_files/GitLogo.png)

# Table of contents
- [Installation](#installation)
  - [Dependencies](#dependencies)
  - [Instrucions](#instrucions)
- [Usage Guide](#usage-guide)
  - [Register and Login](#register-and-login)
  - [Creating a painting](#creating-a-painting)
    - [Drawing tools](#drawing-tools)
    - [Layers](#layers)
    - [Effects](#effects)
  - [Sharing](#sharing)
    - [Profile](#profile)
    - [Friends](#friends)
- [Project Structure](#project-structure)
- [Members](#members)
   
      
---
## Installation
- ### Dependencies
  - #### Core Libraries:  
    Core AndroidX & Compose libraries, AndroidX Splashscreen, Core Material3 Design libraries, Compose Activity and Lifecycle libraries, AndroidX navigation library.
  - #### Databases:  
    Core Room Database libraries, Core Firebase libraries + Firebase Functions library for cloud functions.
  - #### Other:  
    Colorpicker library by skydoves, Core JUnit testing libraries, Gradle
- ### Instrucions
  - #### Android
    Prerequisite: Android phone.   
    Download the latest APK file under [Releases](https://github.com/toomemag/DrawIt/releases)   
    Install the application and open.
    
  - #### Windows
    Download [Android Studio](https://developer.android.com/studio)    
    Download the latest source code file under [Releases](https://github.com/toomemag/DrawIt/releases)    
    Extract files.    
    Open Android Studio and import the folder.    
    Build the application using Gradle ("Assemble 'App' Run Configiration").    
    Create a new simulated device under "Device Manager". For best results, use Android 16.0 ("Baklava").    
    Run the app.    
    
  
## Usage guide 
- ### Register and Login
<img width="385" height="365" alt="image" src="https://github.com/user-attachments/assets/ae936829-af20-4a08-929d-579cfa914fa1" />               

    
The user can register an account using an email and password. The first part of the email becomes the users username. For example creating an account with an email marike@mymail.com will create the username marike.    
After registering an account, the user can log in with the same email and password.              
<img width="267" height="58" alt="image" src="https://github.com/user-attachments/assets/cf9aa4da-2b1f-4499-88d4-021b976d22ff" />             
         
Log out button, next to username.

  
- ### Creating a painting
  <img width="402" height="537" alt="image" src="https://github.com/user-attachments/assets/b52d81d5-8cb7-46ad-8c48-b5f2668137f3" />
         
    Create a new painting using the + symbol.
  <img width="397" height="890" alt="image" src="https://github.com/user-attachments/assets/c6e1c9cb-f066-4a56-8167-f672684a482b" />
       
    The main drawing screen
  
  - ### Drawing tools
   <img width="259" height="58" alt="image" src="https://github.com/user-attachments/assets/5c15bb51-a6a9-4b6a-a447-a966d2233340" />
   
  Different drawing tools, from left to right:
  - **Pencil**: 1 pixel wide drawing tool.
  - **Brush**: Resizable drawing tool, allowing to create bigger brushes.
  - ****

    
  - ### Layers
 
    
  - ### Effects
 
    
- ### Sharing

  
  - ### Profile
 
    
  - ### Friends
 
    
## Project Structure

Todo



## Members
### Erki Toomemägi (toomemag)
### Anton Otrokov (Anton Otrokov)
### Mark Riispapp (markriis, 11x1)
### Romet Vislapuu (Romet-Vislapuu)

## Description
Community drawing app inspired by BeReal. Every day there is a random theme, where you have 10 minutes to draw based on the theme. Also an option to draw whatever you want.
Features are:
1.  Simple and effective drawing - paint, erase, fill, color picker and layers.
2.  Interactive effects - Bring your drawing to life with real time effects, manipulate layers with gyroscope.
3.  Daily Theme - Each day a new theme to work on.
4.  Community feed - See all sorts of drawings made by other artists.
5.  Friend feed - See what your friends have drawn.
  


## Figma Design  
[Figma Design Link](https://www.figma.com/design/5JaJyUQXZNMOg6Y6oCJqzi/Mobdev?node-id=1-2&t=MhjCKBsLWlb4n2oP-1)  
[Figma Prototype](https://www.figma.com/proto/5JaJyUQXZNMOg6Y6oCJqzi/Mobdev?page-id=1%3A2&node-id=4-322&p=f&viewport=1143%2C488%2C0.59&t=PAUF9tpjVYZ7eu66-1&scaling=scale-down&content-scaling=fixed&starting-point-node-id=4%3A2)  
[Readme About Designs](designs_v1/README.md)  
  
  
# General ideas and notes
## Sensor Output to Layer Transformations
![idea image](designs_v1/sensor-output-to-layer-mapping-idea.png)
