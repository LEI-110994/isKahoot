# IsKahoot Game

This project is a simplified, standalone version of the IsKahoot game, focusing on the initial development phases as outlined in the project description. It includes the basic GUI, game state management, and question loading functionalities.

## Prerequisites

- Java 11 or higher
- Apache Maven

## How to Run

1. **Compile the project:**
   ```sh
   mvn compile
   ```

2. **Run the GUI demo:**
   ```sh
   mvn exec:java
   ```

This will start the application and open the game window. You will be prompted to enter a username, and then the game will start with the questions loaded from `resources/questions.json`.
