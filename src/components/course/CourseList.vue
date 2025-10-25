<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
    <h3 class="text-lg font-semibold text-isu-cardinal mb-4">Your Courses</h3>

    <!-- Empty State -->
    <div v-if="courses.length === 0" class="text-center py-8 text-gray-500">
      <i class="bi bi-inbox text-4xl mb-2 block"></i>
      <p>No courses added yet</p>
      <p class="text-sm">Add courses above to build your schedule</p>
    </div>

    <!-- Course Cards -->
    <div v-else class="space-y-3">
      <div
        v-for="course in courses"
        :key="course.courseId"
        class="border border-gray-200 rounded-lg p-3 hover:border-isu-cardinal transition-colors"
      >
        <!-- Course Header -->
        <div class="flex justify-between items-start mb-2">
          <div>
            <h4 class="font-bold text-gray-800">
              {{ course.courseId }}
            </h4>
            <p class="text-sm text-gray-600">{{ course.courseName }}</p>
          </div>
          <button
            @click="$emit('remove-course', course)"
            class="text-red-500 hover:text-red-700 transition-colors"
            title="Remove course"
          >
            <i class="bi bi-trash"></i>
          </button>
        </div>

        <!-- Course Stats -->
        <div class="flex gap-4 text-xs text-gray-600 mb-2">
          <span>
            <i class="bi bi-award"></i> {{ getCourseCredits(course) }} credits
          </span>
          <span>
            <i class="bi bi-people"></i> {{ getSectionsCount(course) }} sections
          </span>
        </div>

        <!-- Toggle Sections Button -->
        <button
          @click="toggleSections(course)"
          class="text-sm text-isu-cardinal hover:underline font-medium"
        >
          <i :class="course.showSections ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
          {{ course.showSections ? 'Hide' : 'Show' }} Sections
        </button>

        <!-- Sections List (Expandable) -->
        <div v-show="course.showSections" class="mt-3 space-y-2">
          <div class="text-xs text-gray-600 mb-2 italic">
            <i class="bi bi-info-circle"></i> By default, all sections are considered. Click to include/exclude specific sections.
          </div>

          <!-- Instructor Preferences (if multiple instructors available) -->
          <div v-if="getUniqueInstructors(course).length > 1" class="mb-3 p-3 bg-isu-gold/10 border border-isu-gold/30 rounded-lg">
            <div class="text-xs font-semibold text-gray-700 mb-2 flex items-center gap-1">
              <i class="bi bi-person-check"></i> Preferred Instructors (optional)
            </div>
            <div class="flex flex-wrap gap-2">
              <label
                v-for="instructor in getUniqueInstructors(course)"
                :key="instructor"
                :class="[
                  'flex items-center gap-1.5 px-2 py-1 text-xs rounded border cursor-pointer transition-colors',
                  isInstructorPreferred(course.courseId, instructor)
                    ? 'bg-isu-cardinal text-white border-isu-cardinal'
                    : 'bg-white text-gray-700 border-gray-300 hover:border-isu-cardinal'
                ]"
              >
                <input
                  type="checkbox"
                  :checked="isInstructorPreferred(course.courseId, instructor)"
                  @change="toggleInstructorPreference(course.courseId, instructor)"
                  class="hidden"
                />
                <span>{{ instructor }}</span>
              </label>
            </div>
            <div class="text-[10px] text-gray-500 mt-1 italic">
              Selecting instructors will prioritize sections taught by them
            </div>
          </div>

          <div
            v-for="section in course.sections"
            :key="`${course.courseId}-${section.sectionNumber}`"
            :class="[
              'rounded p-2 text-sm transition-all cursor-pointer',
              isSectionSelected(course.courseId, section.sectionNumber)
                ? 'bg-green-50 border-2 border-green-500'
                : 'bg-gray-50 border-2 border-transparent hover:border-gray-300'
            ]"
            @click="toggleSection(course.courseId, section)"
          >
            <div class="flex justify-between items-start">
              <div class="flex items-start gap-2">
                <i
                  :class="isSectionSelected(course.courseId, section.sectionNumber)
                    ? 'bi bi-check-circle-fill text-green-600'
                    : 'bi bi-circle text-gray-400'"
                  class="mt-0.5"
                ></i>
                <div>
                  <span class="font-semibold">Section {{ section.sectionNumber }}</span>
                  <span v-if="isOnlineSection(section)" class="ml-2 px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded">
                    Online
                  </span>
                  <span v-if="isTBDSection(section)" class="ml-2 px-2 py-0.5 bg-amber-100 text-amber-700 text-xs rounded">
                    TBD
                  </span>
                  <span class="text-gray-600 ml-2">{{ section.daysOfTheWeek }}</span>
                  <span class="text-gray-600 ml-2">{{ section.timeStart }} - {{ section.timeEnd }}</span>
                  <div class="text-xs text-gray-500 mt-1">
                    {{ section.instructor }} • {{ section.location }}
                    <span v-if="section.openSeats !== undefined" class="ml-2">
                      ({{ section.openSeats }} seats)
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { isOnlineSection, isTBDSection } from '../../utils/sectionUtils.js'

export default {
  name: 'CourseList',

  props: {
    courses: {
      type: Array,
      default: () => []
    },
    selectedSectionIds: {
      type: Object,
      default: () => ({})
    }
  },

  emits: ['remove-course', 'toggle-sections', 'toggle-section-selection', 'instructor-preferences-changed'],

  data() {
    return {
      instructorPreferences: {} // Map of courseId -> array of preferred instructor names
    }
  },

  mounted() {
    // Load instructor preferences from localStorage
    const saved = localStorage.getItem('instructorPreferences')
    if (saved) {
      try {
        this.instructorPreferences = JSON.parse(saved)
      } catch (e) {
        console.error('Failed to load instructor preferences:', e)
        this.instructorPreferences = {}
      }
    }
  },

  methods: {
    isOnlineSection,
    isTBDSection,

    toggleSections(course) {
      // Emit event to parent instead of mutating props directly
      this.$emit('toggle-sections', course.courseId)
    },

    toggleSection(courseId, section) {
      this.$emit('toggle-section-selection', { courseId, section })
    },

    isSectionSelected(courseId, sectionNumber) {
      // If no selections for this course, all sections are implicitly selected
      if (!this.selectedSectionIds[courseId] || this.selectedSectionIds[courseId].length === 0) {
        return false // Show as unselected by default (all are considered)
      }
      // Check if this specific section is in the selected list
      return this.selectedSectionIds[courseId].includes(sectionNumber)
    },

    getCourseCredits(course) {
      // Get credits from first section (all sections have same credits)
      if (course.sections && course.sections.length > 0) {
        return course.sections[0].credits || 0
      }
      return 0
    },

    getSectionsCount(course) {
      return course.sections ? course.sections.length : 0
    },

    getUniqueInstructors(course) {
      if (!course.sections || course.sections.length === 0) {
        return []
      }

      const instructors = new Set()
      course.sections.forEach(section => {
        if (section.instructor && section.instructor !== 'N/A' && section.instructor.trim() !== '') {
          instructors.add(section.instructor)
        }
      })

      return Array.from(instructors).sort()
    },

    isInstructorPreferred(courseId, instructor) {
      return this.instructorPreferences[courseId]?.includes(instructor) || false
    },

    toggleInstructorPreference(courseId, instructor) {
      // Initialize array if it doesn't exist
      if (!this.instructorPreferences[courseId]) {
        this.instructorPreferences[courseId] = []
      }

      const index = this.instructorPreferences[courseId].indexOf(instructor)
      if (index === -1) {
        // Add instructor to preferences
        this.instructorPreferences[courseId].push(instructor)
      } else {
        // Remove instructor from preferences
        this.instructorPreferences[courseId].splice(index, 1)
      }

      // Save to localStorage
      localStorage.setItem('instructorPreferences', JSON.stringify(this.instructorPreferences))

      // Emit changes to parent
      this.$emit('instructor-preferences-changed', this.instructorPreferences)
    }
  }
}
</script>
