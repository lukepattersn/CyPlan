# <img src="https://www.cy-plan.com/images/CyPlan_Logo.png" width="40" height="40"/> CyPlan: Simplifying Course Planning for ISU Students
### Deployed at https://www.cy-plan.com

**CyPlan** is your go-to tool for efficiently organizing your course schedule at Iowa State University. Designed with simplicity and effectiveness in mind, CyPlan helps you manage your academic calendar effortlessly.

## Why CyPlan?

Inspired by the discontinuation of ISU's official schedule generator, the need for a better alternative, and the hate for workday, CyPlan aims to simplify course planning and registration for students. ISU students almost found themselves without a reliable scheduling tool, but CyPlan is here to save the day! Our goal is to provide a streamlined solution that enhances your academic experience.

## Features

With CyPlan, ISU students can:

- Add courses and explore available sections
- Generate optimal, non-overlapping weekly schedules
- Visualize schedules in a clear and intuitive calendar format

## How We Built It

We delved into Iowa State's course catalog, leveraging their internal API to fetch course and section data. Using Java Spring Boot for the backend and HTML, CSS, Bootstrap, and FullCalendar.js for the frontend, we seamlessly integrated this data. JSON responses were parsed using Jackson, ensuring accuracy and dynamism in data representation.

- **Frontend:** HTML5, CSS3, Bootstrap, FullCalendar.js, Vue.js
- **Backend:** Java, Spring Boot
- **APIs:** Iowa State Course API
- **Others:** HTTPS, JSON, Jackson


## Challenges and Triumphs

Integrating and rendering data on FullCalendar.js posed significant challenges, especially parsing complex meeting patterns dynamically. Our team's diverse skill set, with varying levels of experience in Spring Boot and web development, helped us overcome these hurdles. Successfully implementing Iowa State's API and integrating FullCalendar.js were major accomplishments, showcasing our ability to deliver a robust, API-driven application.

## Lessons Learned

CyPlan was not just about building a tool but also about learning and growth. It was our first experience with Spring Boot and a first dive into web development for some team members. This project was a rewarding journey in new frameworks and technologies.

## What's Next for CyPlan

- **AI Integration:** Incorporate AI capabilities to further optimize scheduling based on user preferences.
- **Enhanced Frontend:** Refine the frontend framework further to ensure smoother user interactions and schedule generation.
- **User Preferences:** Add features allowing users to specify desired sections, preferred start/end times, and other scheduling preferences.
- **Schedule Sharing:** Enable users to share their generated schedules with peers and advisors.

## Meet the Team

### [Jeremiah Baccam](https://www.linkedin.com/in/jeremiah-baccam/)
Managed API development, interfacing with Iowa State's data repository. Worked across frontend to backend, also contributing to frontend styling and backend logic.

### [Luke Patterson](https://www.linkedin.com/in/lukepatt/)
Discovered functionality for integrating Iowa State's course API by analyzing HTTP GET and POST requests on the network. Handled frontend development, creating a user-friendly interface using HTML, CSS, Bootstrap, and FullCalendar.js.

### [Jared Cheney](https://www.linkedin.com/in/jared-cheney-68b987296/)
Focused on algorithm development within CyPlan and parsing JSON responses using Jackson, optimizing scheduling generation functionalities for ISU students.

## Installation

If you'd like to run CyPlan locally, follow these steps:

1. **Clone the repository:**

    ```bash
    git clone https://github.com/yourusername/cyplan.git
    ```

    - Replace `yourusername` with the actual GitHub username or organization name where the CyPlan repository is hosted.

2. **Navigate to the project directory:**

    ```bash
    cd cyplan
    ```

3. **Build and Run the Application:**

    - Ensure you have **Java 17** (or the version specified in your project) installed.
    - You do **not** need to have Gradle installed globally; the project uses the Gradle Wrapper.

    - Build the project using the Gradle Wrapper:

        ```bash
        ./gradlew build
        ```

    - Run the Spring Boot application:

        ```bash
        ./gradlew bootRun
        ```

4. **Access CyPlan:**

    - Open your browser and navigate to `http://localhost:8080`.
    
    - Ensure no other applications are running on port `8080`.
  

## Notes
Right now, as of 11/17/2024, CyPlan only generates courses for the Spring semester. A term field will be added in the future but not needed right now. 
