# CyPlan: Simplifying Course Planning for ISU Students

CyPlan is your go-to tool for efficiently organizing your course schedule at Iowa State University. Designed with simplicity and effectiveness in mind, CyPlan helps you manage your academic calendar effortlessly.

## Why CyPlan?

Inspired by the discontinuation of ISU's official schedule generator, the need for a better alternative, and the hate for workday, CyPlan aims to simplify course planning and registration for students. ISU students almost found themselves without a reliable scheduling tool, but CyPlan is here to save the day! Our goal is to provide a streamlined solution that enhances your academic experience.

## Features

With CyPlan, ISU students can:

- Add courses and explore available sections
- Generate optimal, non-overlapping weekly schedules
- Visualize schedules in a clear and intuitive calendar format

## How We Built It

We delved into Iowa State's course catalog, leveraging their internal API to fetch course and section data. Using Java Spring Boot for the backend and HTML, CSS, Bootstrap, and FullCalendar.js for the frontend, we seamlessly integrated this data. JSON responses were parsed using Jackson, ensuring accuracy and dynamism in data representation.

## Challenges and Triumphs

Integrating and rendering data on FullCalendar.js posed significant challenges, especially parsing complex meeting patterns dynamically. Our team's diverse skill set, with varying levels of experience in Spring Boot and web development, helped us overcome these hurdles. Successfully implementing Iowa State's API and integrating FullCalendar.js were major accomplishments, showcasing our ability to deliver a robust, API-driven application.

## Lessons Learned

CyPlan was not just about building a tool but also about learning and growth. It was our first experience with Spring Boot and a first dive into web development for some team members. This project was a rewarding journey in new frameworks and technologies.

## What's Next for CyPlan

In the future, we plan to integrate AI capabilities to further optimize scheduling based on user preferences and constraints. We're also refining our frontend framework to ensure smoother user interactions and schedule generation.

## Built With

- CSS3, HTML5, HTTPS
- Iowa State Course API
- Java, JavaScript
- Spring Boot
- Vue.js 

## Meet the Team

### Jeremiah Baccam
Managed API development, interfacing with Iowa State's data repository. Worked across frontend to backend, also contributing to frontend styling and backend logic.
https://www.linkedin.com/in/jeremiah-baccam/

### Luke Patterson
Handled frontend development, creating a user-friendly interface using HTML, CSS, Bootstrap, and FullCalendar.js. Contributed to backend design and logic for seamless integration.
https://www.linkedin.com/in/lukepatt/

### Jared Cheney
Focused on algorithm development and parsing within CyPlan, optimizing scheduling generation functionalities for ISU students.
https://www.linkedin.com/in/jared-cheney-68b987296/
