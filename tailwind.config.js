/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
    "./src/main/resources/templates/**/*.html"
  ],
  theme: {
    extend: {
      colors: {
        'isu-cardinal': '#C8102E',
        'isu-gold': '#F1BE48',
      },
    },
  },
  plugins: [],
}

