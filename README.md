# 🏛️ Alaska Senate Data Scraper (`alaska-senate-scraper`)

A Java Maven project designed to scrape publicly available contact and professional information for all members of the Alaska State Senate and output the data into a structured JSON file.

## ✨ Features

* **Technology Stack:** Java 11+, Maven, Playwright (for web automation) and Gson (for JSON serialization).
* **Headless Operation:** Runs silently in the background without opening a browser window.
* **Structured Output:** Generates `alaska_senators.json` with clear fields for easy data consumption.
* **Extracted Fields:** Name, Title, Position (District/Role), Party, Address, Phone, Email, and Profile URL.

---

## 🚀 Getting Started

### Prerequisites

1.  **Java Development Kit (JDK):** Version 11 or higher.
2.  **Maven:** Installed and configured on your system.
3.  **IDE:** An Integrated Development Environment like IntelliJ IDEA or VS Code (recommended).
4.  **Internet Connection:** Required for downloading dependencies and scraping the live website.

### Installation & Setup

1.  **Clone the Repository:**
    ```bash
    git clone [YOUR_REPOSITORY_URL]
    cd alaska-senate-scraper
    ```

2.  **Install Playwright Dependencies:**
    Since this project uses Playwright, you need to install the necessary browser drivers. Run this command in your project root:
    ```bash
    mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
    ```
    *(Note: The Playwright dependency is already declared in your `pom.xml`.)*

3.  **Build the Project:**
    Compile and download all Java dependencies (Selenium/Playwright, Gson) defined in `pom.xml`:
    ```bash
    mvn clean install
    ```
    If you are using an IDE (like IntelliJ), ensure you **reload the Maven project** after adding or modifying the `pom.xml`.

---

## ▶️ How to Run

Execute the main application class, `AlaskaSenateWebScraper`, from your IDE or the command line.

### Option 1: Run via IDE (Recommended)

1.  Open the `AlaskaSenateWebScraper.java` file in your IDE.
2.  Locate the `public static void main(String[] args)` method.
3.  Click the green **Run** button/icon next to the `main` method declaration.

### Option 2: Run via Maven Command

You can run the application directly from the command line using the Maven Exec Plugin:

```bash
mvn exec:java -Dexec.mainClass="com.scraper.AlaskaSenateWebScraper"
