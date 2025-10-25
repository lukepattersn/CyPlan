<template>
  <div v-if="hasActiveFilters" class="bg-blue-50 border border-blue-200 rounded-lg p-1.5 sm:p-2 mb-2 sm:mb-3">
    <div class="flex items-start justify-between gap-1.5 sm:gap-2 flex-wrap">
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-1 sm:gap-1.5 mb-1 sm:mb-1.5">
          <i class="bi bi-funnel text-blue-600 text-[9px] sm:text-xs"></i>
          <h4 class="text-[9px] sm:text-xs font-semibold text-blue-900">Active Filters</h4>
        </div>

        <div class="flex flex-wrap gap-1 sm:gap-1.5">
          <!-- Instructor Preference Badges -->
          <div
            v-for="(instructors, courseId) in instructorPreferences"
            :key="'instructor-' + courseId"
          >
            <div
              v-for="instructor in instructors"
              :key="instructor"
              class="inline-flex items-center gap-0.5 sm:gap-1 bg-white border border-blue-300 rounded-full px-1.5 py-0 sm:px-2 sm:py-0.5 text-[8px] sm:text-[9px] leading-none sm:leading-tight h-4 sm:h-auto"
            >
              <i class="bi bi-person-check text-blue-600 flex-shrink-0 text-[8px] sm:text-[9px]"></i>
              <span class="font-medium text-gray-700">{{ courseId }}:</span>
              <span class="text-gray-600">{{ instructor }}</span>
              <button
                @click="removeInstructorFilter(courseId, instructor)"
                class="text-gray-400 hover:text-red-600 transition-colors flex items-center justify-center w-2.5 h-2.5 sm:w-3 sm:h-3 flex-shrink-0 -mr-0.5"
                title="Remove instructor filter"
              >
                <i class="bi bi-x-lg text-[7px] sm:text-[8px]"></i>
              </button>
            </div>
          </div>

          <!-- Section Selection Badges -->
          <div
            v-for="(sections, courseId) in sectionSelections"
            :key="'section-' + courseId"
          >
            <div
              v-for="sectionNumber in sections"
              :key="sectionNumber"
              class="inline-flex items-center gap-0.5 sm:gap-1 bg-white border border-purple-300 rounded-full px-1.5 py-0 sm:px-2 sm:py-0.5 text-[8px] sm:text-[9px] leading-none sm:leading-tight h-4 sm:h-auto"
            >
              <i class="bi bi-check-circle text-purple-600 flex-shrink-0 text-[8px] sm:text-[9px]"></i>
              <span class="font-medium text-gray-700">{{ courseId }}:</span>
              <span class="text-gray-600">Sec {{ sectionNumber }}</span>
              <button
                @click="removeSectionFilter(courseId, sectionNumber)"
                class="text-gray-400 hover:text-red-600 transition-colors flex items-center justify-center w-2.5 h-2.5 sm:w-3 sm:h-3 flex-shrink-0 -mr-0.5"
                title="Remove section filter"
              >
                <i class="bi bi-x-lg text-[7px] sm:text-[8px]"></i>
              </button>
            </div>
          </div>

          <!-- Schedule Preference Badges -->
          <div
            v-if="schedulePreferences.preferredDays && schedulePreferences.preferredDays.length > 0"
            class="inline-flex items-center gap-0.5 sm:gap-1 bg-white border border-green-300 rounded-full px-1.5 py-0 sm:px-2 sm:py-0.5 text-[8px] sm:text-[9px] leading-none sm:leading-tight h-4 sm:h-auto"
          >
            <i class="bi bi-calendar-week text-green-600 flex-shrink-0 text-[8px] sm:text-[9px]"></i>
            <span class="text-gray-600">{{ schedulePreferences.preferredDays.join(', ') }}</span>
            <button
              @click="removePreferredDays"
              class="text-gray-400 hover:text-red-600 transition-colors flex items-center justify-center w-2.5 h-2.5 sm:w-3 sm:h-3 flex-shrink-0 -mr-0.5"
              title="Remove day preference"
            >
              <i class="bi bi-x-lg text-[7px] sm:text-[8px]"></i>
            </button>
          </div>

          <div
            v-if="schedulePreferences.timePreference"
            class="inline-flex items-center gap-0.5 sm:gap-1 bg-white border border-green-300 rounded-full px-1.5 py-0 sm:px-2 sm:py-0.5 text-[8px] sm:text-[9px] leading-none sm:leading-tight h-4 sm:h-auto"
          >
            <i class="bi bi-clock text-green-600 flex-shrink-0 text-[8px] sm:text-[9px]"></i>
            <span class="text-gray-600">{{ formatTimePreference(schedulePreferences.timePreference) }}</span>
            <button
              @click="removeTimePreference"
              class="text-gray-400 hover:text-red-600 transition-colors flex items-center justify-center w-2.5 h-2.5 sm:w-3 sm:h-3 flex-shrink-0 -mr-0.5"
              title="Remove time preference"
            >
              <i class="bi bi-x-lg text-[7px] sm:text-[8px]"></i>
            </button>
          </div>

          <div
            v-if="schedulePreferences.gapPreference"
            class="inline-flex items-center gap-0.5 sm:gap-1 bg-white border border-green-300 rounded-full px-1.5 py-0 sm:px-2 sm:py-0.5 text-[8px] sm:text-[9px] leading-none sm:leading-tight h-4 sm:h-auto"
          >
            <i class="bi bi-pause-circle text-green-600 flex-shrink-0 text-[8px] sm:text-[9px]"></i>
            <span class="text-gray-600">{{ formatGapPreference(schedulePreferences.gapPreference) }}</span>
            <button
              @click="removeGapPreference"
              class="text-gray-400 hover:text-red-600 transition-colors flex items-center justify-center w-2.5 h-2.5 sm:w-3 sm:h-3 flex-shrink-0 -mr-0.5"
              title="Remove gap preference"
            >
              <i class="bi bi-x-lg text-[7px] sm:text-[8px]"></i>
            </button>
          </div>

          <div
            v-if="schedulePreferences.scheduleStyle"
            class="inline-flex items-center gap-0.5 sm:gap-1 bg-white border border-green-300 rounded-full px-1.5 py-0 sm:px-2 sm:py-0.5 text-[8px] sm:text-[9px] leading-none sm:leading-tight h-4 sm:h-auto"
          >
            <i class="bi bi-grid text-green-600 flex-shrink-0 text-[8px] sm:text-[9px]"></i>
            <span class="text-gray-600">{{ formatScheduleStyle(schedulePreferences.scheduleStyle) }}</span>
            <button
              @click="removeScheduleStyle"
              class="text-gray-400 hover:text-red-600 transition-colors flex items-center justify-center w-2.5 h-2.5 sm:w-3 sm:h-3 flex-shrink-0 -mr-0.5"
              title="Remove style preference"
            >
              <i class="bi bi-x-lg text-[7px] sm:text-[8px]"></i>
            </button>
          </div>

          <!-- Unique Schedules Only Badge -->
          <div
            v-if="schedulePreferences.uniqueSchedulesOnly"
            class="inline-flex items-center gap-0.5 sm:gap-1 bg-white border border-amber-300 rounded-full px-1.5 py-0 sm:px-2 sm:py-0.5 text-[8px] sm:text-[9px] leading-none sm:leading-tight h-4 sm:h-auto"
          >
            <i class="bi bi-filter-circle text-amber-600 flex-shrink-0 text-[8px] sm:text-[9px]"></i>
            <span class="text-gray-600">Unique only</span>
            <button
              @click="removeUniqueSchedulesFilter"
              class="text-gray-400 hover:text-red-600 transition-colors flex items-center justify-center w-2.5 h-2.5 sm:w-3 sm:h-3 flex-shrink-0 -mr-0.5"
              title="Show all schedule variations"
            >
              <i class="bi bi-x-lg text-[7px] sm:text-[8px]"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Clear All Button -->
      <button
        @click="clearAllFilters"
        class="flex-shrink-0 text-[8px] sm:text-[9px] text-red-600 hover:text-red-700 font-medium border border-red-300 rounded-full px-2 py-0 sm:px-2.5 sm:py-0.5 hover:bg-red-50 transition-colors whitespace-nowrap h-4 sm:h-auto leading-none sm:leading-tight"
      >
        Clear All
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ActiveFilters',

  props: {
    instructorPreferences: {
      type: Object,
      default: () => ({})
    },
    sectionSelections: {
      type: Object,
      default: () => ({})
    },
    schedulePreferences: {
      type: Object,
      default: () => ({})
    }
  },

  emits: [
    'remove-instructor-filter',
    'remove-section-filter',
    'remove-preferred-days',
    'remove-time-preference',
    'remove-gap-preference',
    'remove-schedule-style',
    'remove-unique-schedules-filter',
    'clear-all-filters'
  ],

  computed: {
    hasActiveFilters() {
      const hasInstructorFilters = Object.keys(this.instructorPreferences).some(
        courseId => this.instructorPreferences[courseId].length > 0
      )
      const hasSectionFilters = Object.keys(this.sectionSelections).some(
        courseId => this.sectionSelections[courseId].length > 0
      )
      const hasSchedulePrefs =
        (this.schedulePreferences.preferredDays && this.schedulePreferences.preferredDays.length > 0) ||
        this.schedulePreferences.timePreference ||
        this.schedulePreferences.gapPreference ||
        this.schedulePreferences.scheduleStyle ||
        this.schedulePreferences.uniqueSchedulesOnly

      return hasInstructorFilters || hasSectionFilters || hasSchedulePrefs
    }
  },

  methods: {
    removeInstructorFilter(courseId, instructor) {
      this.$emit('remove-instructor-filter', { courseId, instructor })
    },

    removeSectionFilter(courseId, sectionNumber) {
      this.$emit('remove-section-filter', { courseId, sectionNumber })
    },

    removePreferredDays() {
      this.$emit('remove-preferred-days')
    },

    removeTimePreference() {
      this.$emit('remove-time-preference')
    },

    removeGapPreference() {
      this.$emit('remove-gap-preference')
    },

    removeScheduleStyle() {
      this.$emit('remove-schedule-style')
    },

    removeUniqueSchedulesFilter() {
      this.$emit('remove-unique-schedules-filter')
    },

    clearAllFilters() {
      this.$emit('clear-all-filters')
    },

    formatTimePreference(value) {
      const map = {
        'morning': 'Morning',
        'afternoon': 'Afternoon',
        'evening': 'Evening'
      }
      return map[value] || value
    },

    formatGapPreference(value) {
      const map = {
        'none': 'Minimize Gaps',
        'short': 'Short Gaps (15-30 min)',
        'any': 'Any Gaps'
      }
      return map[value] || value
    },

    formatScheduleStyle(value) {
      const map = {
        'compact': 'Compact',
        'spread': 'Spread Out',
        'balanced': 'Balanced'
      }
      return map[value] || value
    }
  }
}
</script>
