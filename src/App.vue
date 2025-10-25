<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Flash Message -->
    <FlashMessage
      :message="flashMessage?.message"
      :type="flashMessage?.type || 'info'"
      @dismiss="flashMessage = null"
    />

    <!-- Header -->
    <header class="bg-isu-cardinal text-white shadow-lg">
      <div class="flex items-center justify-between gap-4 py-4">
        <div class="flex items-center gap-3 sm:gap-4 min-w-0 pl-4 sm:pl-6">
          <img src="./main/resources/static/images/CyPlan_Logo.png" alt="ISU Logo" class="w-12 h-12 sm:w-16 sm:h-16 object-contain flex-shrink-0">
          <div class="min-w-0">
            <h1 class="text-2xl sm:text-3xl font-bold whitespace-nowrap">
              <span class="text-isu-gold">Cy</span>Plan
            </h1>
            <p class="text-xs sm:text-sm text-gray-200 hidden sm:block">Iowa State University Class Scheduler</p>
          </div>
        </div>
        <button
          @click="showSavedSchedules = true"
          class="btn-isu-outline border-white text-white hover:bg-white hover:text-isu-cardinal flex-shrink-0 text-sm sm:text-base mr-4 sm:mr-6"
        >
          <i class="bi bi-bookmark-star"></i> <span class="hidden sm:inline">Saved Schedules</span>
        </button>
      </div>
    </header>

    <!-- Main Content -->
    <div class="flex flex-col lg:flex-row min-h-[calc(100vh-100px)]">
      <!-- Desktop Sidebar -->
      <aside
        :class="['transition-all duration-300 ease-in-out', sidebarCollapsed ? 'lg:w-0' : 'lg:w-96']"
        class="hidden lg:flex lg:flex-col bg-white shadow-lg relative overflow-hidden"
      >
        <div v-show="!sidebarCollapsed" class="flex flex-col h-full">
          <!-- Course Search (No scroll, allows dropdown to overflow) -->
          <div class="p-4 flex-shrink-0">
            <CourseSearch
              :departments="departments"
              :academic-periods="academicPeriods"
              :current-period="currentAcademicPeriod"
              @add-course="handleAddCourse"
              @period-change="handlePeriodChange"
              ref="courseSearch"
            />
          </div>

          <!-- Course List (Scrollable) -->
          <div class="flex-1 overflow-y-auto px-4 pb-4 space-y-4">
            <CourseList
              :courses="courses"
              :selected-section-ids="selectedSectionIds"
              @remove-course="handleRemoveCourse"
              @toggle-sections="handleToggleSections"
              @toggle-section-selection="handleToggleSectionSelection"
              @instructor-preferences-changed="handleInstructorPreferencesChanged"
            />

            <!-- Schedule Preferences -->
            <SchedulePreferences
              v-if="courses.length > 0"
              ref="schedulePreferences"
              @preferences-changed="handlePreferencesChanged"
            />
          </div>
        </div>

        <!-- Sidebar Toggle Button (Attached to sidebar edge) -->
        <button
          @click="sidebarCollapsed = !sidebarCollapsed"
          class="absolute top-1/2 -translate-y-1/2 -right-[48px] bg-isu-cardinal text-white py-8 px-3 rounded-r-lg hover:bg-red-700 transition-colors z-40 shadow-xl flex items-center justify-center"
          :title="sidebarCollapsed ? 'Open sidebar' : 'Close sidebar'"
        >
          <i :class="sidebarCollapsed ? 'bi bi-chevron-right text-xl' : 'bi bi-chevron-left text-xl'"></i>
        </button>
      </aside>

      <!-- Mobile Sidebar (Full Screen Overlay) -->
      <transition name="fade">
        <div
          v-if="showMobileSidebar"
          class="lg:hidden fixed inset-0 z-[60] bg-black bg-opacity-50 backdrop-blur-sm"
          @click="showMobileSidebar = false"
        >
          <transition name="slide-up">
            <div
              v-if="showMobileSidebar"
              @click.stop
              class="w-full h-full bg-white overflow-y-auto p-4 space-y-4 animate-slide-up"
            >
              <div class="flex justify-between items-center mb-4 sticky top-0 bg-white pb-3 border-b border-gray-200 z-10">
                <h3 class="text-xl font-bold text-isu-cardinal">Build Schedule</h3>
                <button
                  @click="showMobileSidebar = false"
                  class="text-gray-500 hover:text-gray-700 p-2 hover:bg-gray-100 rounded-lg transition-colors"
                  aria-label="Close sidebar"
                >
                  <i class="bi bi-x-lg text-2xl"></i>
                </button>
              </div>

              <!-- Course Search -->
              <CourseSearch
                :departments="departments"
                :academic-periods="academicPeriods"
                :current-period="currentAcademicPeriod"
                @add-course="handleAddCourse"
                @period-change="handlePeriodChange"
                ref="courseSearchMobile"
              />

              <!-- Course List -->
              <CourseList
                :courses="courses"
                :selected-section-ids="selectedSectionIds"
                @remove-course="handleRemoveCourse"
                @toggle-sections="handleToggleSections"
                @toggle-section-selection="handleToggleSectionSelection"
                @instructor-preferences-changed="handleInstructorPreferencesChanged"
              />

              <!-- Schedule Preferences -->
              <SchedulePreferences
                v-if="courses.length > 0"
                ref="schedulePreferencesMobile"
                @preferences-changed="handlePreferencesChanged"
                class="mt-4"
              />
            </div>
          </transition>
        </div>
      </transition>

      <!-- Mobile FAB Button (Fixed Action Button) - Toggles based on modal state -->
      <button
        @click="showMobileSidebar = !showMobileSidebar"
        class="lg:hidden fixed bottom-6 right-6 text-white rounded-full shadow-2xl active:scale-95 transition-all z-[70] flex items-center gap-2 px-5 py-4"
        :class="showMobileSidebar ? 'bg-gray-600 hover:bg-gray-700' : 'bg-isu-cardinal hover:bg-red-700 hover:shadow-isu-cardinal/50'"
        :aria-label="showMobileSidebar ? 'Close courses' : 'Add courses'"
      >
        <i :class="showMobileSidebar ? 'bi bi-x-lg text-xl sm:text-2xl' : 'bi bi-plus-lg text-xl sm:text-2xl'"></i>
        <span class="font-semibold text-sm sm:text-base">{{ showMobileSidebar ? 'Close' : 'Add Courses' }}</span>
      </button>


      <!-- Main Calendar Area -->
      <main class="flex-1 p-2 sm:p-4 lg:p-6">
        <div class="bg-white rounded-lg shadow-lg p-3 sm:p-6">
          <div class="mb-6">
            <h2 class="text-2xl font-bold text-isu-cardinal">Your Schedule</h2>
          </div>

          <!-- Active Filters -->
          <ActiveFilters
            :instructor-preferences="instructorPreferences"
            :section-selections="selectedSectionIds"
            :schedule-preferences="schedulePreferences"
            @remove-instructor-filter="handleRemoveInstructorFilter"
            @remove-section-filter="handleRemoveSectionFilter"
            @remove-preferred-days="handleRemovePreferredDays"
            @remove-time-preference="handleRemoveTimePreference"
            @remove-gap-preference="handleRemoveGapPreference"
            @remove-schedule-style="handleRemoveScheduleStyle"
            @remove-unique-schedules-filter="handleRemoveUniqueSchedulesFilter"
            @clear-all-filters="handleClearAllFilters"
          />

          <!-- Schedule Navigation Controls (Compact) -->
          <div
            v-if="scheduleCount > 0"
            class="flex items-center justify-between border border-gray-200 rounded-lg px-3 py-2 mb-4 bg-white"
          >
            <!-- Navigation -->
            <div class="flex items-center gap-2">
              <button
                @click="handlePreviousSchedule"
                class="text-isu-cardinal hover:bg-gray-100 rounded p-1 transition-colors"
                :disabled="loading"
              >
                <i class="bi bi-chevron-left text-lg"></i>
              </button>
              <span class="text-sm font-medium text-gray-700">
                {{ currentScheduleIndex + 1 }} / {{ scheduleCount }}
              </span>
              <button
                @click="handleNextSchedule"
                class="text-isu-cardinal hover:bg-gray-100 rounded p-1 transition-colors"
                :disabled="loading"
              >
                <i class="bi bi-chevron-right text-lg"></i>
              </button>
            </div>

            <!-- Save Button -->
            <div>
              <button
                v-if="!showSaveInput"
                @click="showSaveInput = true"
                class="flex items-center gap-1.5 bg-isu-gold text-gray-800 px-3 py-1.5 rounded text-sm hover:bg-yellow-500 transition-colors"
              >
                <i class="bi bi-bookmark-plus text-sm"></i>
                <span>Save</span>
              </button>
              <div v-else class="flex gap-2">
                <input
                  v-model="scheduleNameInput"
                  type="text"
                  placeholder="Schedule name"
                  class="w-40 px-2 py-1 border border-gray-300 rounded text-sm focus:ring-1 focus:ring-isu-cardinal focus:border-transparent"
                  @keydown.enter="handleSaveSchedule"
                  @keydown.esc="showSaveInput = false; scheduleNameInput = ''"
                />
                <button
                  @click="handleSaveSchedule"
                  class="bg-isu-cardinal text-white px-2 py-1 rounded hover:bg-red-700 transition-colors text-sm"
                >
                  <i class="bi bi-check"></i>
                </button>
                <button
                  @click="showSaveInput = false; scheduleNameInput = ''"
                  class="px-2 py-1 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 transition-colors text-sm"
                >
                  <i class="bi bi-x"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- Calendar -->
          <CalendarGrid
            :sections="inPersonSections"
            :hour-height="80"
            @course-click="handleCourseClick"
          />

          <!-- Daily Class List -->
          <div class="mt-6">
            <DailyClassList :sections="selectedSections" />
          </div>
        </div>
      </main>
    </div>

    <!-- Saved Schedules Modal -->
    <div
      v-if="showSavedSchedules"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[80] p-4"
      @click.self="showSavedSchedules = false"
    >
      <div class="bg-white rounded-lg shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h3 class="text-2xl font-bold text-isu-cardinal">Saved Schedules</h3>
          <button
            @click="showSavedSchedules = false"
            class="text-gray-500 hover:text-gray-700"
          >
            <i class="bi bi-x-lg text-2xl"></i>
          </button>
        </div>
        <div class="p-6">
          <!-- Empty State -->
          <div v-if="savedSchedules.length === 0" class="text-center py-8 text-gray-500">
            <i class="bi bi-bookmark-x text-4xl mb-2 block"></i>
            <p>No saved schedules yet</p>
            <p class="text-sm">Save your favorite schedules to access them later</p>
          </div>

          <!-- Saved Schedules List -->
          <div v-else class="space-y-4">
            <div
              v-for="schedule in savedSchedules"
              :key="schedule.id"
              class="border border-gray-200 rounded-lg p-4 hover:border-isu-cardinal transition-colors"
            >
              <div class="flex justify-between items-start mb-2">
                <div>
                  <h4 class="font-bold text-gray-800">{{ schedule.name }}</h4>
                  <p class="text-xs text-gray-500">Saved on {{ schedule.timestamp }}</p>
                </div>
                <button
                  @click="handleDeleteSchedule(schedule.id)"
                  class="text-red-500 hover:text-red-700 transition-colors"
                  title="Delete schedule"
                >
                  <i class="bi bi-trash"></i>
                </button>
              </div>
              <div class="text-sm text-gray-600 mb-3">
                <i class="bi bi-book"></i> {{ schedule.courses.length }} courses
              </div>
              <button
                @click="handleLoadSchedule(schedule)"
                class="w-full bg-isu-cardinal text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors text-sm font-medium"
              >
                <i class="bi bi-arrow-clockwise"></i> Load This Schedule
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="bg-gray-50 border-t border-gray-200 py-6 text-center text-xs text-gray-500">
      <div class="max-w-7xl mx-auto px-4">
        <p class="mb-1">© 2025 CyPlan. All rights reserved.</p>
        <p class="mb-2">Not directly affiliated with Iowa State University</p>
        <p class="text-gray-400">Created by Jeremiah Baccam, Luke Patterson, Jared Cheney</p>
      </div>
    </footer>
  </div>
</template>

<script>
import CalendarGrid from './components/calendar/CalendarGrid.vue'
import DailyClassList from './components/calendar/DailyClassList.vue'
import CourseSearch from './components/course/CourseSearch.vue'
import CourseList from './components/course/CourseList.vue'
import SchedulePreferences from './components/preferences/SchedulePreferences.vue'
import FlashMessage from './components/ui/FlashMessage.vue'
import ActiveFilters from './components/ui/ActiveFilters.vue'
import { shouldShowInCalendar } from './utils/sectionUtils.js'

export default {
  name: 'App',

  components: {
    CalendarGrid,
    DailyClassList,
    CourseSearch,
    CourseList,
    SchedulePreferences,
    FlashMessage,
    ActiveFilters
  },

  data() {
    return {
      // Server data (will be injected by Thymeleaf)
      departments: window.serverData?.departments || [],
      academicPeriods: window.serverData?.academicPeriods || [],
      currentAcademicPeriod: window.serverData?.selectedAcademicPeriod || '',
      courses: window.serverData?.courses?.map(c => ({
        ...c,
        showSections: false
      })) || [],
      selectedSections: window.serverData?.selectedSections || [],
      currentScheduleIndex: window.serverData?.currentScheduleIndex || 0,
      scheduleCount: window.serverData?.scheduleCount || 0,

      // UI state
      sidebarCollapsed: false,
      showSavedSchedules: false,
      showMobileSidebar: false,
      savedSchedules: [],
      loading: false,
      flashMessage: null,
      scheduleNameInput: '',
      showSaveInput: false,
      selectedSectionIds: {}, // Map of courseId -> array of selected section numbers
      instructorPreferences: {}, // Map of courseId -> array of preferred instructor names
      schedulePreferences: {} // Schedule preferences including uniqueSchedulesOnly
    }
  },

  mounted() {
    // Load saved schedules from localStorage
    this.loadSavedSchedules()

    // Check for TBD sections and notify user
    this.checkForTBDSections()

    // Load instructor preferences from localStorage
    const savedInstructorPrefs = localStorage.getItem('instructorPreferences')
    if (savedInstructorPrefs) {
      try {
        this.instructorPreferences = JSON.parse(savedInstructorPrefs)
      } catch (e) {
        console.error('Failed to load instructor preferences:', e)
      }
    }

    // Load selected section IDs from localStorage
    const savedSectionIds = localStorage.getItem('selectedSectionIds')
    if (savedSectionIds) {
      try {
        this.selectedSectionIds = JSON.parse(savedSectionIds)
      } catch (e) {
        console.error('Failed to load selected section IDs:', e)
      }
    }

    // Load schedule preferences from localStorage
    const savedSchedulePrefs = localStorage.getItem('schedulePreferences')
    if (savedSchedulePrefs) {
      try {
        this.schedulePreferences = JSON.parse(savedSchedulePrefs)
      } catch (e) {
        console.error('Failed to load schedule preferences:', e)
        // Set default if parsing fails
        this.schedulePreferences = { uniqueSchedulesOnly: false }
      }
    } else {
      // Set default preferences if none saved
      this.schedulePreferences = {
        preferredDays: [],
        timePreference: '',
        gapPreference: '',
        scheduleStyle: '',
        uniqueSchedulesOnly: false
      }
    }
  },

  computed: {
    academicPeriodName() {
      const period = this.academicPeriods.find(p => p.code === this.currentAcademicPeriod);
      return period ? period.description : 'Fall 2024';
    },

    // Filter out non-schedulable courses for calendar display
    // Only in-person sections with valid times should appear in the calendar grid
    inPersonSections() {
      return this.selectedSections.filter(section => shouldShowInCalendar(section))
    }
  },

  methods: {
    handleCourseClick(section) {
      console.log('Course clicked:', section)
      // TODO: Show course details modal
    },

    async handleAddCourse(courseData) {
      this.loading = true

      const params = new URLSearchParams()
      params.append('courseSubject', courseData.courseSubject)
      params.append('courseNumber', courseData.courseNumber)
      if (courseData.academicPeriodId) {
        params.append('academicPeriodId', courseData.academicPeriodId)
      }

      console.log('Sending add course request:', {
        courseSubject: courseData.courseSubject,
        courseNumber: courseData.courseNumber,
        academicPeriodId: courseData.academicPeriodId
      })

      try {
        const response = await fetch('/addCourse', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include',
          body: params
        })

        console.log('Response status:', response.status)
        const data = await response.json()
        console.log('Response data:', data)

        if (data.success) {
          this.showFlashMessage(data.message, 'success')

          // Reload page to get updated course data
          setTimeout(() => {
            window.location.reload()
          }, 800)
        } else {
          this.showFlashMessage(data.message, 'danger')
          this.$refs.courseSearch?.setError(data.message)
          this.$refs.courseSearchMobile?.setError(data.message)
        }
      } catch (error) {
        console.error('Error adding course:', error)
        this.showFlashMessage('Error adding course: ' + error.message, 'danger')
      } finally {
        this.loading = false
      }
    },

    async handleRemoveCourse(course) {
      if (!confirm(`Remove ${course.courseId}?`)) {
        return
      }

      this.loading = true

      const params = new URLSearchParams()
      params.append('courseId', course.courseId)

      try {
        const response = await fetch('/removeCourse', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include',
          body: params
        })

        const data = await response.json()

        if (data.success) {
          // Clear any filters associated with this course before reloading
          if (this.instructorPreferences[course.courseId]) {
            delete this.instructorPreferences[course.courseId]
            localStorage.setItem('instructorPreferences', JSON.stringify(this.instructorPreferences))
          }

          if (this.selectedSectionIds[course.courseId]) {
            delete this.selectedSectionIds[course.courseId]
            localStorage.setItem('selectedSectionIds', JSON.stringify(this.selectedSectionIds))
          }

          this.showFlashMessage(data.message, 'success')
          window.location.reload()
        } else {
          this.showFlashMessage(data.message, 'danger')
        }
      } catch (error) {
        console.error('Error removing course:', error)
        this.showFlashMessage('Error removing course', 'danger')
      } finally {
        this.loading = false
      }
    },

    handleToggleSectionSelection({ courseId, section }) {
      // Initialize array for this course if it doesn't exist
      if (!this.selectedSectionIds[courseId]) {
        this.selectedSectionIds[courseId] = []
      }

      const sectionNumber = section.sectionNumber
      const index = this.selectedSectionIds[courseId].indexOf(sectionNumber)

      if (index === -1) {
        // Add section to selection
        this.selectedSectionIds[courseId].push(sectionNumber)
      } else {
        // Remove section from selection
        this.selectedSectionIds[courseId].splice(index, 1)
      }

      // Save to localStorage
      localStorage.setItem('selectedSectionIds', JSON.stringify(this.selectedSectionIds))

      // Auto-expand preferences when section selection changes
      this.expandPreferences()

      // Auto-regenerate schedules with filtered sections
      this.regenerateWithSelectedSections()
    },

    async regenerateWithSelectedSections() {
      if (this.courses.length === 0) return

      this.loading = true
      this.showFlashMessage('Regenerating schedules with selected sections...', 'info')

      try {
        const params = new URLSearchParams()

        // Send selected section IDs to backend
        if (Object.keys(this.selectedSectionIds).length > 0) {
          // Build a map of courseId -> [sectionIds in format "courseId-sectionNumber"]
          const sectionFilters = {}
          for (const [courseId, sectionNumbers] of Object.entries(this.selectedSectionIds)) {
            if (sectionNumbers && sectionNumbers.length > 0) {
              // Generate section IDs in format "courseId-sectionNumber"
              const sectionIds = sectionNumbers.map(sectionNumber => `${courseId}-${sectionNumber}`)
              sectionFilters[courseId] = sectionIds
            }
          }

          if (Object.keys(sectionFilters).length > 0) {
            params.append('selectedSections', JSON.stringify(sectionFilters))
          }
        }

        // Also send instructor preferences if any
        if (Object.keys(this.instructorPreferences).length > 0) {
          params.append('instructorPreferences', JSON.stringify(this.instructorPreferences))
        }

        // Call generate schedules endpoint
        const response = await fetch('/generateSchedules', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include',
          body: params
        })

        if (response.ok) {
          window.location.reload()
        } else {
          this.showFlashMessage('Error regenerating schedules', 'danger')
        }
      } catch (error) {
        console.error('Error regenerating schedules:', error)
        this.showFlashMessage('Error regenerating schedules', 'danger')
      } finally {
        this.loading = false
      }
    },

    async handleGenerateSchedules() {
      this.loading = true
      this.showFlashMessage('Generating schedules...', 'info')

      try {
        const response = await fetch('/generateSchedules', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include'
        })

        if (response.ok) {
          window.location.reload()
        } else {
          this.showFlashMessage('Error generating schedules', 'danger')
        }
      } catch (error) {
        console.error('Error generating schedules:', error)
        this.showFlashMessage('Error generating schedules', 'danger')
      } finally {
        this.loading = false
      }
    },

    async handlePeriodChange(period) {
      this.loading = true

      const params = new URLSearchParams()
      params.append('academicPeriod', period)

      try {
        const response = await fetch('/changeAcademicPeriod', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include',
          body: params
        })

        if (response.ok) {
          window.location.reload()
        } else {
          this.showFlashMessage('Error changing period', 'danger')
        }
      } catch (error) {
        console.error('Error changing period:', error)
        this.showFlashMessage('Error changing period', 'danger')
      } finally {
        this.loading = false
      }
    },

    showFlashMessage(message, type) {
      this.flashMessage = { message, type }
      setTimeout(() => {
        this.flashMessage = null
      }, 3000)
    },

    handleToggleSections(courseId) {
      const course = this.courses.find(c => c.courseId === courseId)
      if (course) {
        course.showSections = !course.showSections
      }
    },

    handleInstructorPreferencesChanged(preferences) {
      this.instructorPreferences = preferences

      // Auto-expand preferences when instructor preference changes
      this.expandPreferences()

      // Auto-regenerate schedules with new instructor preferences
      if (this.courses.length > 0) {
        this.regenerateWithPreferences()
      }
    },

    async regenerateWithPreferences() {
      if (this.courses.length === 0) return

      this.loading = true
      this.showFlashMessage('Applying your preferences...', 'info')

      const params = new URLSearchParams()

      // Send instructor preferences
      if (Object.keys(this.instructorPreferences).length > 0) {
        params.append('instructorPreferences', JSON.stringify(this.instructorPreferences))
      }

      try {
        const response = await fetch('/generateSchedules', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include',
          body: params
        })

        if (response.ok) {
          this.showFlashMessage('Schedules updated with your preferences!', 'success')
          setTimeout(() => {
            window.location.reload()
          }, 500)
        } else {
          this.showFlashMessage('Error applying preferences', 'danger')
        }
      } catch (error) {
        console.error('Error applying preferences:', error)
        this.showFlashMessage('Error applying preferences', 'danger')
      } finally {
        this.loading = false
      }
    },

    async handleNextSchedule() {
      this.loading = true
      try {
        const response = await fetch('/nextSchedule', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include'
        })

        if (response.ok) {
          window.location.reload()
        } else {
          this.showFlashMessage('Error navigating schedules', 'danger')
        }
      } catch (error) {
        console.error('Error navigating to next schedule:', error)
        this.showFlashMessage('Error navigating schedules', 'danger')
      } finally {
        this.loading = false
      }
    },

    async handlePreviousSchedule() {
      this.loading = true
      try {
        const response = await fetch('/previousSchedule', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include'
        })

        if (response.ok) {
          window.location.reload()
        } else {
          this.showFlashMessage('Error navigating schedules', 'danger')
        }
      } catch (error) {
        console.error('Error navigating to previous schedule:', error)
        this.showFlashMessage('Error navigating schedules', 'danger')
      } finally {
        this.loading = false
      }
    },

    async handlePreferencesChanged(preferences) {
      // Store preferences in component state
      this.schedulePreferences = preferences

      // Auto-expand preferences panel when changed
      this.expandPreferences()

      this.loading = true
      this.showFlashMessage('Regenerating schedules with your preferences...', 'info')

      const params = new URLSearchParams()

      // Format schedule preferences as JSON string for backend
      const preferencesData = {
        preferredDays: preferences.preferredDays || [],
        timePreference: preferences.timePreference || '',
        gapPreference: preferences.gapPreference || '',
        scheduleStyle: preferences.scheduleStyle || '',
        uniqueSchedulesOnly: preferences.uniqueSchedulesOnly !== false // Default to true
      }

      params.append('preferences', JSON.stringify(preferencesData))

      // Also send instructor preferences if any are set
      if (Object.keys(this.instructorPreferences).length > 0) {
        params.append('instructorPreferences', JSON.stringify(this.instructorPreferences))
      }

      try {
        const response = await fetch('/generateSchedules', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          credentials: 'include',
          body: params
        })

        if (response.ok) {
          this.showFlashMessage('Schedules regenerated with your preferences!', 'success')
          setTimeout(() => {
            window.location.reload()
          }, 500)
        } else {
          this.showFlashMessage('Error applying preferences', 'danger')
        }
      } catch (error) {
        console.error('Error applying preferences:', error)
        this.showFlashMessage('Error applying preferences', 'danger')
      } finally {
        this.loading = false
      }
    },

    loadSavedSchedules() {
      const saved = localStorage.getItem('cyplan_saved_schedules')
      if (saved) {
        try {
          this.savedSchedules = JSON.parse(saved)
        } catch (e) {
          console.error('Failed to load saved schedules:', e)
          this.savedSchedules = []
        }
      }
    },

    handleSaveSchedule() {
      if (!this.scheduleNameInput.trim()) {
        this.showFlashMessage('Please enter a schedule name', 'warning')
        return
      }

      const schedule = {
        id: Date.now().toString(),
        name: this.scheduleNameInput.trim(),
        timestamp: new Date().toLocaleString(),
        courses: this.courses,
        sections: this.selectedSections,
        academicPeriod: this.currentAcademicPeriod,
        scheduleIndex: this.currentScheduleIndex
      }

      this.savedSchedules.push(schedule)
      localStorage.setItem('cyplan_saved_schedules', JSON.stringify(this.savedSchedules))

      this.showFlashMessage(`Schedule "${schedule.name}" saved successfully!`, 'success')
      this.showSaveInput = false
      this.scheduleNameInput = ''
    },

    handleDeleteSchedule(id) {
      if (!confirm('Are you sure you want to delete this saved schedule?')) {
        return
      }

      this.savedSchedules = this.savedSchedules.filter(s => s.id !== id)
      localStorage.setItem('cyplan_saved_schedules', JSON.stringify(this.savedSchedules))
      this.showFlashMessage('Schedule deleted successfully', 'success')
    },

    async handleLoadSchedule(schedule) {
      this.showFlashMessage('Loading schedule...', 'info')
      this.loading = true

      try {
        // Clear current session and reload with saved schedule data
        // This would require backend support to restore courses and generate schedules
        // For now, just show the courses and sections
        this.courses = schedule.courses
        this.selectedSections = schedule.sections
        this.currentAcademicPeriod = schedule.academicPeriod
        this.currentScheduleIndex = schedule.scheduleIndex || 0
        this.showSavedSchedules = false

        this.showFlashMessage('Schedule loaded! Add or remove courses to regenerate.', 'success')
      } catch (error) {
        console.error('Error loading schedule:', error)
        this.showFlashMessage('Error loading schedule', 'danger')
      } finally {
        this.loading = false
      }
    },

    // Filter Management Methods
    handleRemoveInstructorFilter({ courseId, instructor }) {
      if (this.instructorPreferences[courseId]) {
        const index = this.instructorPreferences[courseId].indexOf(instructor)
        if (index > -1) {
          this.instructorPreferences[courseId].splice(index, 1)

          // Remove the course key if no instructors left
          if (this.instructorPreferences[courseId].length === 0) {
            delete this.instructorPreferences[courseId]
          }

          // Save and regenerate
          localStorage.setItem('instructorPreferences', JSON.stringify(this.instructorPreferences))
          this.regenerateWithPreferences()
        }
      }
    },

    handleRemoveSectionFilter({ courseId, sectionNumber }) {
      if (this.selectedSectionIds[courseId]) {
        const index = this.selectedSectionIds[courseId].indexOf(sectionNumber)
        if (index > -1) {
          this.selectedSectionIds[courseId].splice(index, 1)

          // Remove the course key if no sections left
          if (this.selectedSectionIds[courseId].length === 0) {
            delete this.selectedSectionIds[courseId]
          }

          // Save and regenerate
          localStorage.setItem('selectedSectionIds', JSON.stringify(this.selectedSectionIds))
          this.regenerateWithSelectedSections()
        }
      }
    },

    handleRemovePreferredDays() {
      this.schedulePreferences.preferredDays = []
      this.updateSchedulePreferencesAndRegenerate()
    },

    handleRemoveTimePreference() {
      this.schedulePreferences.timePreference = ''
      this.updateSchedulePreferencesAndRegenerate()
    },

    handleRemoveGapPreference() {
      this.schedulePreferences.gapPreference = ''
      this.updateSchedulePreferencesAndRegenerate()
    },

    handleRemoveScheduleStyle() {
      this.schedulePreferences.scheduleStyle = ''
      this.updateSchedulePreferencesAndRegenerate()
    },

    handleRemoveUniqueSchedulesFilter() {
      this.schedulePreferences.uniqueSchedulesOnly = false
      this.updateSchedulePreferencesAndRegenerate()
    },

    handleClearAllFilters() {
      // Clear all instructor preferences
      this.instructorPreferences = {}
      localStorage.removeItem('instructorPreferences')

      // Clear all section selections
      this.selectedSectionIds = {}
      localStorage.removeItem('selectedSectionIds')

      // Clear all schedule preferences
      this.schedulePreferences = {
        preferredDays: [],
        timePreference: '',
        gapPreference: '',
        scheduleStyle: '',
        uniqueSchedulesOnly: false
      }
      localStorage.removeItem('schedulePreferences')

      // Regenerate schedules
      this.handleGenerateSchedules()
    },

    updateSchedulePreferencesAndRegenerate() {
      // Save to localStorage
      localStorage.setItem('schedulePreferences', JSON.stringify(this.schedulePreferences))

      // Trigger preferences changed handler
      this.handlePreferencesChanged(this.schedulePreferences)
    },

    expandPreferences() {
      // Expand both desktop and mobile preference panels
      if (this.$refs.schedulePreferences) {
        this.$refs.schedulePreferences.expand()
      }
      if (this.$refs.schedulePreferencesMobile) {
        this.$refs.schedulePreferencesMobile.expand()
      }
    },

    checkForTBDSections() {
      // Check if any sections in the current schedule have TBD time or days
      const tbdSections = this.selectedSections.filter(section =>
        section.daysOfTheWeek === 'TBD' ||
        section.timeStart === 'TBD' ||
        section.timeEnd === 'TBD'
      )

      if (tbdSections.length > 0) {
        // Get unique course IDs with TBD sections
        const tbdCourses = [...new Set(tbdSections.map(s => s.courseId))]
        const courseList = tbdCourses.join(', ')

        this.showFlashMessage(
          `Note: ${courseList} ${tbdCourses.length === 1 ? 'has' : 'have'} sections with TBD meeting times/days. Check the TBD tab for details.`,
          'warning'
        )
      }
    }
  }
}
</script>
