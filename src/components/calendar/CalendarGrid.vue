<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200">
    <!-- Calendar Header -->
    <div class="px-2 sm:px-4 py-3 border-b border-gray-200">
      <h3 class="text-base sm:text-lg font-semibold text-gray-800">Weekly Schedule</h3>
    </div>

    <!-- Calendar Body - No vertical scroll -->
    <div class="p-2 sm:p-4 overflow-x-hidden">
      <!-- Day Headers -->
      <div :class="['grid gap-0 mb-2', gridColsClass]">
        <div class=""></div>
        <div
          v-for="day in weekDays"
          :key="`header-${day}`"
          class="bg-gradient-to-b from-isu-cardinal to-red-700 text-white text-center py-2 sm:py-3 font-semibold border-b-2 border-gray-300 text-xs sm:text-sm"
        >
          {{ day.substring(0, 3) }}
        </div>
      </div>

      <!-- Calendar Grid with Events -->
      <div class="relative">
        <!-- Background Grid -->
        <div :class="['grid gap-0', gridColsClass]">
          <template v-for="(hour, index) in timeSlots" :key="`slot-${hour}`">
            <!-- Time Label -->
            <div
              class="text-right pr-1 sm:pr-2 pt-1 text-[10px] sm:text-xs text-gray-500 border-r border-gray-200 w-[40px] sm:w-[60px]"
              :style="{ height: responsiveHourHeight + 'px' }"
            >
              {{ formatHour(hour) }}
            </div>

            <!-- Day Cells -->
            <div
              v-for="day in weekDays"
              :key="`${day}-${hour}`"
              class="border-r border-b border-gray-100 relative bg-white"
              :style="{ height: responsiveHourHeight + 'px' }"
            ></div>
          </template>
        </div>

        <!-- Course Events Overlay -->
        <div
          v-for="event in calendarEvents"
          :key="event.id"
          class="absolute rounded shadow-lg border-l-4 overflow-hidden cursor-pointer transition-all hover:shadow-xl hover:z-20 touch-manipulation"
          :style="event.style"
          @click="$emit('course-click', event.section)"
        >
          <div class="h-full px-1.5 sm:px-2 py-1 sm:py-1.5 text-white text-[10px] sm:text-xs" :style="{ backgroundColor: event.color }">
            <div class="font-bold leading-tight">{{ event.title }}</div>
            <div class="opacity-90 mt-0.5 hidden sm:block">{{ event.time }}</div>
            <div class="opacity-75 mt-0.5 text-[9px] sm:text-xs hidden sm:block">{{ event.location }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { timeToMinutes } from '../../utils/timeUtils'

export default {
  name: 'CalendarGrid',

  props: {
    sections: {
      type: Array,
      default: () => []
    },
    hourHeight: {
      type: Number,
      default: 80
    }
  },

  emits: ['course-click'],

  data() {
    return {
      weekDays: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'],
      windowWidth: typeof window !== 'undefined' ? window.innerWidth : 1024
    }
  },

  mounted() {
    // Add window resize listener for responsive behavior
    if (typeof window !== 'undefined') {
      window.addEventListener('resize', this.handleResize)
      this.handleResize()
    }
  },

  beforeUnmount() {
    if (typeof window !== 'undefined') {
      window.removeEventListener('resize', this.handleResize)
    }
  },

  computed: {
    // Responsive grid columns class
    gridColsClass() {
      return 'grid-cols-[40px_repeat(5,1fr)] sm:grid-cols-[60px_repeat(5,1fr)]'
    },

    // Responsive hour height based on screen size
    responsiveHourHeight() {
      if (this.windowWidth < 640) {
        return Math.max(50, this.hourHeight * 0.75) // Smaller on mobile, min 50px for touch
      } else if (this.windowWidth < 1024) {
        return Math.max(65, this.hourHeight * 0.85) // Medium on tablet
      }
      return this.hourHeight // Full size on desktop
    },

    // Responsive max height for calendar
    maxCalendarHeight() {
      if (this.windowWidth < 640) {
        return 'calc(100vh - 300px)' // More space for mobile controls
      } else if (this.windowWidth < 1024) {
        return 'calc(100vh - 250px)' // Medium on tablet
      }
      return 'calc(100vh - 200px)' // Desktop
    },

    // Calculate time range based on classes
    timeRange() {
      if (!this.sections || this.sections.length === 0) {
        return { min: 8, max: 17 }
      }

      let minTime = 24 * 60
      let maxTime = 0

      this.sections.forEach(section => {
        if (section.timeStart && section.timeStart !== 'N/A' && section.timeStart !== 'TBD' && section.timeStart !== 'Online') {
          minTime = Math.min(minTime, timeToMinutes(section.timeStart))
        }
        if (section.timeEnd && section.timeEnd !== 'N/A' && section.timeEnd !== 'TBD' && section.timeEnd !== 'Online') {
          maxTime = Math.max(maxTime, timeToMinutes(section.timeEnd))
        }
      })

      if (minTime > maxTime) return { min: 8, max: 17 }

      // Add 1 hour padding
      let minHour = Math.max(0, Math.floor(minTime / 60) - 1)
      let maxHour = Math.min(23, Math.ceil(maxTime / 60) + 1)

      return { min: minHour, max: maxHour }
    },

    timeSlots() {
      const slots = []
      for (let hour = this.timeRange.min; hour <= this.timeRange.max; hour++) {
        slots.push(hour)
      }
      return slots
    },

    totalHeight() {
      return this.timeSlots.length * this.hourHeight
    },

    // Generate positioned events for each course section
    calendarEvents() {
      const events = []
      const colors = ['#C8102E', '#2E86AB', '#A23B72', '#F18F01', '#06A77D', '#8338EC']

      // Create a stable color mapping based on unique courseIds
      const courseColorMap = {}
      const uniqueCourseIds = [...new Set(this.sections.map(s => s.courseId))]
      uniqueCourseIds.forEach((courseId, index) => {
        courseColorMap[courseId] = colors[index % colors.length]
      })

      this.sections.forEach((section) => {
        if (!section.daysOfTheWeek || section.daysOfTheWeek === 'Online' || section.daysOfTheWeek === 'N/A' || section.daysOfTheWeek === 'TBD') {
          return
        }

        if (!section.timeStart || !section.timeEnd || section.timeStart === 'N/A' || section.timeStart === 'TBD' || section.timeStart === 'Online') {
          return
        }

        const days = section.daysOfTheWeek.split(',').map(d => d.trim())
        const startMinutes = timeToMinutes(section.timeStart)
        const endMinutes = timeToMinutes(section.timeEnd)
        const duration = endMinutes - startMinutes

        // Calculate position from top using responsive hour height
        const minutesFromStart = startMinutes - (this.timeRange.min * 60)
        const top = (minutesFromStart / 60) * this.responsiveHourHeight
        const height = Math.max(44, (duration / 60) * this.responsiveHourHeight) // Min 44px for touch targets

        const dayMap = {
          'Mon': 'Monday',
          'Tue': 'Tuesday',
          'Wed': 'Wednesday',
          'Thu': 'Thursday',
          'Fri': 'Friday'
        }

        days.forEach(dayAbbr => {
          const fullDay = dayMap[dayAbbr] || dayAbbr
          const dayIndex = this.weekDays.indexOf(fullDay)

          if (dayIndex === -1) return

          // Calculate left position - responsive for mobile/desktop
          const timeColWidth = this.windowWidth < 640 ? '40px' : '60px'
          const columnWidth = `calc((100% - ${timeColWidth}) / 5)`
          const left = `calc(${timeColWidth} + ${columnWidth} * ${dayIndex})`

          events.push({
            id: `${section.sectionId}-${dayAbbr}`,
            section: section,
            title: section.courseId || `${section.courseSubject || ''} ${section.courseNumber || ''}`.trim() || 'Course',
            time: `${section.timeStart} - ${section.timeEnd}`,
            location: section.building || section.location || 'TBA',
            color: courseColorMap[section.courseId] || colors[0], // Use courseId-based color
            style: {
              top: `${top}px`,
              left: left,
              width: columnWidth,
              height: `${height}px`,
              minHeight: '44px', // Touch-friendly minimum
              paddingLeft: '4px',
              paddingRight: '4px'
            }
          })
        })
      })

      return events
    }
  },

  methods: {
    formatHour(hour) {
      if (hour === 0) return '12 AM'
      if (hour === 12) return '12 PM'
      if (hour > 12) return `${hour - 12} PM`
      return `${hour} AM`
    },

    handleResize() {
      this.windowWidth = window.innerWidth
    }
  }
}
</script>
