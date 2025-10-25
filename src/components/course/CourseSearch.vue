<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
    <h3 class="text-lg font-semibold text-isu-cardinal mb-4">Add a Course</h3>

    <!-- Academic Period Selector -->
    <div class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-2">
        Academic Period
      </label>
      <select
        v-model="selectedPeriod"
        :disabled="loadingPeriods"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-isu-cardinal focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
      >
        <option value="">{{ loadingPeriods ? 'Loading periods...' : 'Select Period' }}</option>
        <option v-for="period in allAcademicPeriods" :key="period.code" :value="period.code">
          {{ period.description }}
        </option>
      </select>
    </div>

    <!-- Department Search (Autocomplete) -->
    <div class="mb-4 relative">
      <label class="block text-sm font-medium text-gray-700 mb-2">
        Department <span class="text-red-500">*</span>
      </label>
      <input
        v-model="departmentInput"
        @input="filterDepartments"
        @focus="showDropdown = true"
        @blur="hideDropdown"
        @keydown.enter="selectFirstDepartment"
        :disabled="loadingDepartments || !selectedPeriod"
        type="text"
        :placeholder="loadingDepartments ? 'Loading departments...' : !selectedPeriod ? 'Select a period first' : 'Type to search (e.g., COMS, SE)'"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-isu-cardinal focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
        :class="{ 'border-red-500': showError && !departmentInput }"
      />

      <!-- Dropdown -->
      <div
        v-show="showDropdown && filteredDepartments.length > 0"
        class="absolute z-[100] w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
      >
        <button
          v-for="dept in filteredDepartments"
          :key="dept"
          @mousedown="selectDepartment(dept)"
          class="w-full text-left px-4 py-2 hover:bg-isu-cardinal hover:text-white transition-colors border-b border-gray-100 last:border-0"
        >
          {{ dept }}
        </button>
      </div>
    </div>

    <!-- Course Number -->
    <div class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-2">
        Course Number <span class="text-red-500">*</span>
      </label>
      <input
        ref="courseNumberInput"
        v-model="courseNumber"
        @keydown.enter="handleAddCourse"
        type="text"
        placeholder="e.g., 227"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-isu-cardinal focus:border-transparent"
        :class="{ 'border-red-500': showError && !courseNumber }"
      />
    </div>

    <!-- Add Course Button -->
    <button
      @click="handleAddCourse"
      :disabled="loading"
      class="w-full btn-isu flex items-center justify-center gap-2"
      :class="{ 'opacity-50 cursor-not-allowed': loading }"
    >
      <i class="bi bi-plus-circle"></i>
      {{ loading ? 'Adding...' : 'Add Course' }}
    </button>

    <!-- Error Message -->
    <div v-if="errorMessage" class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
      <i class="bi bi-exclamation-triangle"></i> {{ errorMessage }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'CourseSearch',

  props: {
    departments: {
      type: Array,
      default: () => []
    },
    academicPeriods: {
      type: Array,
      default: () => []
    },
    currentPeriod: {
      type: String,
      default: ''
    }
  },

  emits: ['add-course', 'period-change'],

  data() {
    return {
      departmentInput: '',
      courseNumber: '',
      selectedPeriod: this.currentPeriod,
      filteredDepartments: [],
      showDropdown: false,
      showError: false,
      errorMessage: '',
      loading: false,
      departmentJustSelected: false,
      allAcademicPeriods: [],
      allDepartments: [],
      loadingPeriods: false,
      loadingDepartments: false,
      isInitialLoad: true
    }
  },

  mounted() {
    this.fetchAcademicPeriods()
  },

  watch: {
    currentPeriod(newVal) {
      this.selectedPeriod = newVal
    },
    selectedPeriod(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        // Always fetch departments when period changes
        this.fetchDepartments(newVal)
        // Don't emit period-change on initial auto-select
        // Period change is only needed if we want to reload the page
        // For now, just fetching departments is enough
      }
    }
  },

  methods: {
    async fetchAcademicPeriods() {
      this.loadingPeriods = true
      try {
        const response = await fetch('/api/academic-periods', {
          credentials: 'include'
        })
        const data = await response.json()
        this.allAcademicPeriods = data.data.map(period => ({
          code: period.id,
          description: period.name
        }))

        // Set current period to the first one that is current, or the first one
        const currentPeriod = data.data.find(p => p.isCurrent)
        if (currentPeriod && !this.selectedPeriod) {
          this.selectedPeriod = currentPeriod.id
          // Mark initial load as complete after first auto-select
          setTimeout(() => {
            this.isInitialLoad = false
          }, 100)
        } else {
          this.isInitialLoad = false
        }
      } catch (error) {
        console.error('Error fetching academic periods:', error)
        this.errorMessage = 'Failed to load academic periods'
        this.isInitialLoad = false
      } finally {
        this.loadingPeriods = false
      }
    },

    async fetchDepartments(academicPeriodId) {
      this.loadingDepartments = true
      try {
        const response = await fetch(`/api/departments?academicPeriod=${academicPeriodId}`, {
          credentials: 'include'
        })
        const data = await response.json()
        this.allDepartments = data.data
        this.filteredDepartments = data.data.slice(0, 10)
      } catch (error) {
        console.error('Error fetching departments:', error)
        this.errorMessage = 'Failed to load departments'
      } finally {
        this.loadingDepartments = false
      }
    },

    filterDepartments() {
      if (this.departmentJustSelected) {
        this.departmentInput = ''
        this.departmentJustSelected = false
      }

      const departmentList = this.allDepartments.length > 0 ? this.allDepartments : this.departments
      const input = this.departmentInput.toUpperCase()
      if (!input) {
        this.filteredDepartments = departmentList.slice(0, 10)
      } else {
        const exactCodeMatches = []
        const partialCodeMatches = []
        const nameMatches = []

        departmentList.forEach(dept => {
          const deptUpper = dept.toUpperCase()
          const deptCode = dept.split(/[\s-]/)[0].toUpperCase()

          if (deptCode === input) {
            exactCodeMatches.push(dept)
          } else if (deptCode.startsWith(input)) {
            partialCodeMatches.push(dept)
          } else if (deptUpper.includes(input)) {
            nameMatches.push(dept)
          }
        })

        this.filteredDepartments = [...exactCodeMatches, ...partialCodeMatches, ...nameMatches].slice(0, 10)
      }
      this.showDropdown = this.filteredDepartments.length > 0
      this.errorMessage = ''
    },

    selectDepartment(dept) {
      this.departmentInput = dept
      this.showDropdown = false
      this.departmentJustSelected = true
      this.errorMessage = ''

      // Focus course number input using ref
      this.$nextTick(() => {
        this.$refs.courseNumberInput?.focus()
      })
    },

    selectFirstDepartment(event) {
      event.preventDefault()
      if (this.filteredDepartments.length > 0) {
        this.selectDepartment(this.filteredDepartments[0])
      }
    },

    hideDropdown() {
      setTimeout(() => {
        this.showDropdown = false
      }, 200)
    },

    handleAddCourse() {
      this.showError = true
      this.errorMessage = ''

      if (!this.departmentInput || !this.courseNumber) {
        this.errorMessage = 'Please fill in both department and course number'
        return
      }

      if (!this.selectedPeriod) {
        this.errorMessage = 'Please select an academic period'
        return
      }

      // Find the full department name from the list
      // User might type "COMS" but we need to send "COMS - Computer Science"
      const departmentList = this.allDepartments.length > 0 ? this.allDepartments : this.departments
      const inputUpper = this.departmentInput.toUpperCase()

      let fullDepartmentName = this.departmentInput

      // If user just typed a code (e.g., "COMS"), find the full name
      for (const dept of departmentList) {
        const deptCode = dept.split(/[\s-]/)[0].toUpperCase()
        if (deptCode === inputUpper || dept.toUpperCase() === inputUpper) {
          fullDepartmentName = dept
          break
        }
      }

      console.log('Adding course:', {
        courseSubject: fullDepartmentName,
        courseNumber: this.courseNumber,
        academicPeriodId: this.selectedPeriod
      })

      this.loading = true
      this.$emit('add-course', {
        courseSubject: fullDepartmentName,
        courseNumber: this.courseNumber,
        academicPeriodId: this.selectedPeriod
      })

      // Reset after emit (parent will handle success/error)
      setTimeout(() => {
        this.loading = false
      }, 500)
    },

    reset() {
      this.departmentInput = ''
      this.courseNumber = ''
      this.showError = false
      this.errorMessage = ''
    },

    setError(message) {
      this.errorMessage = message
      this.loading = false
    }
  }
}
</script>
