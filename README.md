# <p align=center>DrawIt
![img1](https://github.com/toomemag/DrawIt/blob/main/ReadMe_files/GitLogo.png)

# Table of contents
- [Description](#description)
- [Installation](#installation)
  - [Dependencies](#dependencies)
  - [Instrucions](#instrucions)
- [Usage Guide](#usage-guide)
  - [Register and Login](#register-and-login)
  - [Creating a painting](#creating-a-painting)
    - [Drawing tools](#drawing-tools)
    - [Layers](#layers)
    - [Effects](#effects)
    - [Saving](#saving)
  - [Sharing](#sharing)
    - [Publishing](#publishing)
    - [Profile](#profile)
    - [Friends](#friends)
- [Project Structure](#project-structure)
  - [Members](#members)
   


## Description
  Community drawing app inspired by BeReal. Every day there is a random theme, where you have 10 minutes to draw based on the theme. Also an option to draw whatever you want.    
  Features are:    
  1.  Simple and effective drawing - paint, erase, fill, color picker and layers.    
  2.  Interactive effects - Bring your drawing to life with real time effects, manipulate layers with gyroscope.    
  3.  Daily Theme - Each day a new theme to work on.    
  4.  Community feed - See all sorts of drawings made by other artists.    
  5.  Friend feed - See what your friends have drawn.    

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
         
    Create a new painting using the "+" symbol.  
  <img width="397" height="890" alt="image" src="https://github.com/user-attachments/assets/c6e1c9cb-f066-4a56-8167-f672684a482b" />
       
    The main drawing screen
  
  - ### Drawing tools
   <img width="259" height="58" alt="image" src="https://github.com/user-attachments/assets/5c15bb51-a6a9-4b6a-a447-a966d2233340" />
   
  Different drawing tools, from left to right:
    - **Pencil**: 1 pixel wide drawing tool.
    - **Brush**: Resizable drawing tool, allowing to create bigger brushes.
    - **Fill**: Fills a closed area with a single colour.
    - **Eraser**: Erases pixels.
    - **Color Palette**: From here the user can choose colors and the transparency.
 
 
    
  - ### Layers
    <img width="287" height="107" alt="image" src="https://github.com/user-attachments/assets/84cef868-e7a7-4e17-b1db-7f998340f2c4" />

    A layer must be selected, in order to paint on the layer. Layers can be added using the "+" symbol.

    ![LayerOrder](https://github.com/user-attachments/assets/3434ba71-5344-4634-beb5-fdbf23bb57f1)
  
    Layer draw order can be changed, by holding down the current layer and changing its position.

    
  - ### Effects
    <img width="342" height="215" alt="image" src="https://github.com/user-attachments/assets/099bd0ce-13fc-4ce9-8ccf-a8dbc53ef9d3" />

    Each layer can have different effects applied on them. Effects are sensors, that detect changes made to the user's phone, like phone rotation.

    <img width="325" height="398" alt="image" src="https://github.com/user-attachments/assets/b21f4725-96c3-419d-99ed-010e1d3e077a" />

    Each effect can change the property of a currently active layer.
    
    Currently all effects can change the layers X position, Y position, layer rotation, layer scale and layer's transparency value.

    Effects can be removed from layers.

  - ### Saving
    <img width="359" height="378" alt="image" src="https://github.com/user-attachments/assets/be5ab6bf-739f-4630-9a18-a6de7054ce77" />

    Images can be saving after pressing the back button or the "<" icon. Images saved this way will be saved under "Drafts".
 
    
    <img width="401" height="195" alt="image" src="https://github.com/user-attachments/assets/c76eb778-e0a1-41ba-a42a-d54eec116e0c" />
   
    
- ### Sharing
  - ### Publishing
    <img width="344" height="383" alt="image" src="https://github.com/user-attachments/assets/8fbed57f-c0a3-4879-8184-5d0bcf0740c1" />

    After the user is finished with their painting, they have the option to publish their work of art.

  - ### Profile
    <img width="389" height="887" alt="image" src="https://github.com/user-attachments/assets/21ea8053-8387-4fb0-8590-ff0bcdc98400" />

    Profile page shows the users drafts and uploaded drawings.

  - ### Friends
   <img width="388" height="888" alt="image" src="https://github.com/user-attachments/assets/26212c20-455e-4f6c-8f5b-48a8bfd01410" />

   From freinds tab the user can send friend requests, accept on deny other people requests and get general information about their friends.

    
## Project Structure

- ## Members
  ### Erki Toomemägi (toomemag)
  ### Anton Otrokov (Anton Otrokov)
  ### Mark Riispapp (markriis, 11x1)
  ### Romet Vislapuu (Romet-Vislapuu)


  


## Figma Design  
[Figma Design Link](https://www.figma.com/design/5JaJyUQXZNMOg6Y6oCJqzi/Mobdev?node-id=1-2&t=MhjCKBsLWlb4n2oP-1)  
[Figma Prototype](https://www.figma.com/proto/5JaJyUQXZNMOg6Y6oCJqzi/Mobdev?page-id=1%3A2&node-id=4-322&p=f&viewport=1143%2C488%2C0.59&t=PAUF9tpjVYZ7eu66-1&scaling=scale-down&content-scaling=fixed&starting-point-node-id=4%3A2)  
[Readme About Designs](designs_v1/README.md)  
  
  
# General ideas and notes
## Sensor Output to Layer Transformations
![idea image](designs_v1/sensor-output-to-layer-mapping-idea.png)
