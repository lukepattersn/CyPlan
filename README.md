# <img src="https://www.cy-plan.com/images/CyPlan_Logo.png" width="40" height="40"/> CyPlan: Simplifying Course Planning for ISU Students

### Deployed at https://www.cy-plan.com

**CyPlan** is your go-to tool for efficiently organizing your course schedule at Iowa State University. Designed with simplicity and effectiveness in mind, CyPlan helps you manage your academic calendar effortlessly.

## Why CyPlan?

Inspired by the discontinuation of ISU's official schedule generator, the need for a better alternative, and the hate for workday, CyPlan aims to simplify course planning and registration for students. ISU students almost found themselves without a reliable scheduling tool, but CyPlan is here to save the day! Our goal is to provide a streamlined solution that enhances your academic experience.

## Features

With CyPlan, ISU students can:

- **Course Management:** Add courses and explore all available sections
- **Smart Schedule Generation:** Generate optimal, non-overlapping weekly schedules automatically
- **Visual Calendar:** Visualize schedules in a clear and intuitive calendar format
- **Schedule Preferences:** Customize schedules based on:
  - Preferred days of the week
  - Time preferences (morning, afternoon, evening)
  - Gap preferences between classes
  - Schedule style (compact, spread out, balanced)
- **Advanced Filtering:**
  - Filter by preferred instructors
  - Select specific sections
  - View unique schedules only (hide location/instructor variations)
- **Multiple Semesters:** Support for all academic periods (Fall, Spring, Summer)
- **Online & TBD Courses:** Special handling for online courses and sections with TBD meeting times
- **Save Schedules:** Save and load your favorite schedule configurations
- **Mobile Responsive:** Fully functional on desktop and mobile devices

## How We Built It

We leveraged Iowa State's internal course catalog API to fetch real-time course and section data. The application features a modern tech stack with Java Spring Boot powering the backend and Vue.js 3 with Tailwind CSS on the frontend.

- **Frontend:** Vue.js 3, Vite, Tailwind CSS, HTML5, CSS3
- **Backend:** Java, Spring Boot
- **APIs:** Iowa State Course Catalog API
- **Build Tools:** Gradle, npm
- **Data Processing:** Jackson for JSON parsing

## Challenges and Triumphs

Building an intelligent scheduling algorithm that handles various constraints (time conflicts, commute times, required lab/recitation pairings) posed significant challenges. Integrating Vue.js with Spring Boot's server-side rendering and managing complex state across schedule preferences required careful architecture planning. Successfully implementing Iowa State's API, creating a responsive UI, and delivering a robust scheduling engine were major accomplishments.

## Lessons Learned

CyPlan was not just about building a tool but also about learning and growth. The project involved mastering modern frontend frameworks (Vue.js), deepening our understanding of Spring Boot, and learning how to build complex scheduling algorithms that handle real-world constraints.

## What's Next for CyPlan

- **AI Integration:** Incorporate AI capabilities to predict optimal schedules based on historical data and user patterns
- **Enhanced Preferences:** Add more granular preferences like building preferences, walking distance calculations
- **Schedule Sharing:** Enable users to share their generated schedules with peers and advisors
- **Rate My Professor Integration:** Show professor ratings directly in the interface
- **Export Options:** Export schedules to various formats (PDF, iCal, Google Calendar)

## Meet the Team

### [Jeremiah Baccam](https://www.linkedin.com/in/jeremiah-baccam/)
Managed API development, interfacing with Iowa State's data repository. Worked across frontend to backend, also contributing to frontend styling and backend logic.

### [Luke Patterson](https://www.linkedin.com/in/lukepatt/)
Discovered functionality for integrating Iowa State's course API by analyzing HTTP GET and POST requests on the network. Led frontend development, creating a modern user interface using Vue.js, Tailwind CSS, and responsive design principles.

### [Jared Cheney](https://www.linkedin.com/in/jared-cheney-68b987296/)
Focused on algorithm development within CyPlan and parsing JSON responses using Jackson, optimizing scheduling generation functionalities for ISU students.

## Installation

If you'd like to run CyPlan locally, follow these steps:

1. **Clone the repository:**

    ```bash
    git clone https://github.com/lukepattersn/CyPlan.git
    ```

2. **Navigate to the project directory:**

    ```bash
    cd CyPlan
    ```

3. **Install frontend dependencies:**

    ```bash
    npm install
    ```

4. **Build the frontend:**

    ```bash
    npm run build
    ```

5. **Build and run the application:**

    - Ensure you have **Java 17** (or later) installed.
    - You do **not** need to have Gradle installed globally; the project uses the Gradle Wrapper.

    - Build the project:

        ```bash
        ./gradlew build
        ```

    - Run the Spring Boot application:

        ```bash
        ./gradlew bootRun
        ```

6. **Access CyPlan:**

    - Open your browser and navigate to `http://localhost:8080`

    - Ensure no other applications are running on port `8080`

## Development

For frontend development with hot-reload:

```bash
npm run dev
```

This runs the Vite dev server on `http://localhost:5173`. The Spring Boot backend should still run on `http://localhost:8080`.

## License

This project is for educational purposes. Not officially affiliated with Iowa State University.
