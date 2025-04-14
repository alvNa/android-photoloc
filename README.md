# Android Photo Location App

This Android application demonstrates how to extract GPS location data from photos using the MVP (Model-View-Presenter) architecture pattern.

## Architecture Overview

The app follows the MVP (Model-View-Presenter) architecture pattern, which provides a clear separation of concerns:

### 1. Model Layer
- **PhotoLocation.kt**: Data class that represents the location information extracted from a photo
  - Properties: `latitude`, `longitude`, and `hasValidLocation` (computed property)
  - Located in: `app/src/main/java/com/example/cursordemo/model/`

### 2. Presenter Layer
- **PhotoLocationPresenter.kt**: Contains the interface and implementation for handling photo location logic
  - Interface: `PhotoLocationPresenter` with method `getPhotoLocation(photoUri: Uri): PhotoLocation`
  - Implementation: `PhotoLocationPresenterImpl` that uses coroutines to extract GPS coordinates
  - Located in: `app/src/main/java/com/example/cursordemo/presenter/`

### 3. View Layer
- **MainActivity.kt**: The main UI component that handles user interactions and displays results
  - Uses the presenter to process photos
  - Handles permissions and image selection
  - Displays location results via Toast messages
  - Located in: `app/src/main/java/com/example/cursordemo/`

### 4. Service Layer
- **PhotoLocationService.kt**: Service class that handles the actual extraction of GPS coordinates from photos
  - Contains the core logic for reading EXIF data from images
  - Located in: `app/src/main/java/com/example/cursordemo/services/`

## Key Components

### MainActivity
- Entry point of the application
- Handles UI interactions and permissions
- Uses coroutines for asynchronous operations
- Communicates with the presenter to process photos

### PhotoLocationPresenter
- Acts as a bridge between the View and Model layers
- Handles business logic for photo processing
- Uses coroutines to perform background operations
- Returns a PhotoLocation object to the View

### PhotoLocation
- Data class representing location information
- Contains latitude and longitude coordinates
- Includes a computed property to check if location data is valid

## UI Components
- Simple interface with a centered button for photo selection
- Toast messages for displaying results and errors
- Permission handling for accessing external storage

## Flow of Data
1. User taps "Select Photo" button
2. App checks for storage permissions
3. User selects a photo from the gallery
4. MainActivity passes the photo URI to the presenter
5. Presenter uses the service to extract location data
6. Results are returned to MainActivity and displayed to the user

## Technical Details
- Uses Kotlin coroutines for asynchronous operations
- Implements proper permission handling for Android storage access
- Follows clean architecture principles with clear separation of concerns
- Uses Android's ActivityResultContracts for handling activity results
