<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
    <h3 class="text-lg sm:text-xl font-semibold text-isu-cardinal mb-4">Classes by Day</h3>

    <!-- Empty State -->
    <div v-if="!hasClasses" class="text-center py-8 text-gray-500">
      <i class="bi bi-calendar-x text-4xl mb-2 block"></i>
      <p>No classes scheduled yet</p>
      <p class="text-sm">Add courses to see your weekly schedule</p>
    </div>

    <!-- Daily Schedule -->
    <div v-else class="grid gap-4" :class="gridColsClass" style="grid-auto-rows: min-content; align-items: start;">
      <div
        v-for="day in visibleDays"
        :key="day"
        class="border rounded-lg overflow-hidden w-full"
        :class="{
          'border-amber-300': day === 'TBD',
          'border-blue-300': day === 'Online',
          'border-gray-200': !['TBD', 'Online'].includes(day)
        }"
        :style="{ height: collapsedDays[day] ? 'auto' : 'auto' }"
      >
        <!-- Day Header (Clickable) -->
        <button
          @click="collapsedDays[day] = !collapsedDays[day]"
          class="w-full text-white text-center py-2 font-semibold transition-colors flex items-center justify-between px-4"
          :class="{
            'bg-amber-600 hover:bg-amber-700': day === 'TBD',
            'bg-blue-600 hover:bg-blue-700': day === 'Online',
            'bg-isu-cardinal hover:bg-red-700': !['TBD', 'Online'].includes(day)
          }"
        >
          <span>{{ day }}</span>
          <i :class="collapsedDays[day] ? 'bi bi-chevron-down' : 'bi bi-chevron-up'" class="text-sm"></i>
        </button>

        <!-- Classes for this day (Collapsible) -->
        <div v-show="!collapsedDays[day]" class="p-3">
          <div v-if="classesByDay[day] && classesByDay[day].length > 0" class="space-y-3">
          <div
            v-for="classItem in classesByDay[day]"
            :key="classItem.id"
            class="border-l-4 pl-2 py-1"
            :style="{ borderColor: classItem.color }"
          >
            <div class="font-semibold text-sm text-gray-800">
              {{ classItem.courseId }}
            </div>
            <div class="text-xs text-gray-600 mt-1">
              <i class="bi bi-clock"></i> {{ classItem.time }}
            </div>
            <div class="text-xs text-gray-600">
              <i class="bi bi-geo-alt"></i> {{ classItem.location }}
            </div>
            <div class="text-xs text-gray-600">
              <i class="bi bi-person"></i> {{ classItem.instructor }}
            </div>
          </div>
          </div>

          <!-- No classes for this day -->
          <div v-else class="text-center py-4 text-gray-400 text-sm">
            No classes
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DailyClassList',

  props: {
    sections: {
      type: Array,
      default: () => []
    }
  },

  data() {
    return {
      weekDays: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Online', 'TBD'],
      collapsedDays: {} // Track which days are collapsed
    }
  },

  computed: {
    hasClasses() {
      return this.sections && this.sections.length > 0
    },

    classesByDay() {
      const dayMap = {
        'Mon': 'Monday',
        'Tue': 'Tuesday',
        'Wed': 'Wednesday',
        'Thu': 'Thursday',
        'Fri': 'Friday'
      }

      const colors = ['#C8102E', '#2E86AB', '#A23B72', '#F18F01', '#06A77D', '#8338EC']

      // Create color map by courseId for consistency
      const courseColorMap = {}
      const uniqueCourseIds = [...new Set(this.sections.map(s => s.courseId))]
      uniqueCourseIds.forEach((courseId, index) => {
        courseColorMap[courseId] = colors[index % colors.length]
      })

      // Organize classes by day
      const organized = {
        'Monday': [],
        'Tuesday': [],
        'Wednesday': [],
        'Thursday': [],
        'Friday': [],
        'Online': [],
        'TBD': []
      }

      this.sections.forEach(section => {
        // Handle Online sections first
        if (section.daysOfTheWeek === 'Online' ||
            section.deliveryMode === 'Online' ||
            (section.daysOfTheWeek === 'N/A' && section.deliveryMode === 'Online')) {
          organized['Online'].push({
            id: `${section.sectionId || section.courseId}-Online`,
            courseId: section.courseId,
            time: 'Online - No set meeting time',
            location: section.location || 'Online',
            instructor: section.instructor || 'TBA',
            color: courseColorMap[section.courseId] || colors[0]
          })
          return
        }

        // Handle TBD sections (in-person courses with missing time or day)
        if (section.daysOfTheWeek === 'TBD' ||
            section.timeStart === 'TBD' ||
            section.timeEnd === 'TBD') {
          organized['TBD'].push({
            id: `${section.sectionId || section.courseId}-TBD`,
            courseId: section.courseId,
            time: (section.timeStart === 'TBD' || section.timeEnd === 'TBD') ? 'Time TBD' : (section.timeStart && section.timeEnd ? `${section.timeStart} - ${section.timeEnd}` : 'Time TBD'),
            location: section.location || 'TBA',
            instructor: section.instructor || 'TBA',
            color: courseColorMap[section.courseId] || colors[0]
          })
          return
        }

        // Skip sections without valid time/day info (not online, not TBD, just missing data)
        if (!section.daysOfTheWeek ||
            section.daysOfTheWeek === 'N/A' ||
            !section.timeStart ||
            !section.timeEnd ||
            section.timeStart === 'N/A') {
          return
        }

        const days = section.daysOfTheWeek.split(',').map(d => d.trim())

        days.forEach(dayAbbr => {
          const fullDay = dayMap[dayAbbr] || dayAbbr
          if (organized[fullDay]) {
            organized[fullDay].push({
              id: `${section.sectionId}-${dayAbbr}`,
              courseId: section.courseId,
              time: `${section.timeStart} - ${section.timeEnd}`,
              location: section.location || 'TBA',
              instructor: section.instructor || 'TBA',
              color: courseColorMap[section.courseId] || colors[0]
            })
          }
        })
      })

      // Sort classes by start time for each day
      Object.keys(organized).forEach(day => {
        organized[day].sort((a, b) => {
          const timeA = this.timeToMinutes(a.time.split(' - ')[0])
          const timeB = this.timeToMinutes(b.time.split(' - ')[0])
          return timeA - timeB
        })
      })

      return organized
    },

    visibleDays() {
      // Show Online and TBD tabs only if they have classes
      const hasOnlineClasses = this.classesByDay['Online'] && this.classesByDay['Online'].length > 0
      const hasTBDClasses = this.classesByDay['TBD'] && this.classesByDay['TBD'].length > 0

      return this.weekDays.filter(day => {
        // Always show Mon-Fri
        if (['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'].includes(day)) {
          return true
        }
        // Show Online tab only if there are online classes
        if (day === 'Online') {
          return hasOnlineClasses
        }
        // Show TBD tab only if there are TBD classes
        if (day === 'TBD') {
          return hasTBDClasses
        }
        return false
      })
    },

    gridColsClass() {
      // Adjust grid columns based on visible days (Mon-Fri + Online + TBD)
      const visibleCount = this.visibleDays.length

      if (visibleCount === 7) {
        // Mon-Fri + Online + TBD
        return 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-7'
      } else if (visibleCount === 6) {
        // Mon-Fri + either Online or TBD
        return 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6'
      } else {
        // Mon-Fri only
        return 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5'
      }
    }
  },

  methods: {
    timeToMinutes(time) {
      if (!time || time === 'N/A' || time === 'TBD' || time === 'Time TBD' || time === 'Online' || time.startsWith('Online -')) return 0
      const parts = time.match(/(\d+):(\d+)\s*(AM|PM)/i)
      if (!parts) return 0
      let hours = parseInt(parts[1])
      const minutes = parseInt(parts[2])
      const period = parts[3].toUpperCase()
      if (period === 'PM' && hours !== 12) hours += 12
      if (period === 'AM' && hours === 12) hours = 0
      return hours * 60 + minutes
    }
  }
}
</script>
