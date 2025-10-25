<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
    <button
      @click="expanded = !expanded"
      class="w-full flex items-center justify-between text-left"
    >
      <div class="flex items-center gap-2">
        <i class="bi bi-sliders text-isu-cardinal"></i>
        <h3 class="text-lg font-semibold text-isu-cardinal">Schedule Preferences</h3>
      </div>
      <i :class="expanded ? 'bi bi-chevron-up' : 'bi bi-chevron-down'" class="text-gray-500"></i>
    </button>

    <div v-show="expanded" class="mt-4 space-y-4">
      <!-- Preferred Days -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          <i class="bi bi-calendar-week"></i> Preferred Days
        </label>
        <div class="flex flex-wrap gap-2">
          <label
            v-for="day in daysOfWeek"
            :key="day"
            class="flex items-center gap-2 px-3 py-2 border rounded-lg cursor-pointer transition-colors"
            :class="preferences.preferredDays.includes(day) ? 'bg-isu-cardinal text-white border-isu-cardinal' : 'bg-white text-gray-700 border-gray-300 hover:border-isu-cardinal'"
          >
            <input
              type="checkbox"
              :value="day"
              v-model="preferences.preferredDays"
              class="hidden"
            />
            <span class="text-sm">{{ day }}</span>
          </label>
        </div>
      </div>

      <!-- Time Preference -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          <i class="bi bi-clock"></i> Time Preference
        </label>
        <select
          v-model="preferences.timePreference"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-isu-cardinal focus:border-transparent"
        >
          <option value="">No Preference</option>
          <option value="morning">Morning Classes (Before 12 PM)</option>
          <option value="afternoon">Afternoon Classes (12 PM - 5 PM)</option>
          <option value="evening">Evening Classes (After 5 PM)</option>
        </select>
      </div>

      <!-- Gap Preference -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          <i class="bi bi-pause-circle"></i> Gap Between Classes
        </label>
        <select
          v-model="preferences.gapPreference"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-isu-cardinal focus:border-transparent"
        >
          <option value="">No Preference</option>
          <option value="none">Minimize Gaps (Back-to-back classes)</option>
          <option value="short">Allow Short Gaps (15-30 min)</option>
          <option value="any">Any Gaps (No preference)</option>
        </select>
      </div>

      <!-- Schedule Style -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          <i class="bi bi-grid"></i> Schedule Style
        </label>
        <select
          v-model="preferences.scheduleStyle"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-isu-cardinal focus:border-transparent"
        >
          <option value="">No Preference</option>
          <option value="compact">Compact (Fewer days, longer days)</option>
          <option value="spread">Spread Out (More days, shorter days)</option>
          <option value="balanced">Balanced (Even distribution)</option>
        </select>
      </div>

      <!-- Unique Schedules Only -->
      <div class="border-t border-gray-200 pt-4">
        <label class="flex items-start gap-3 cursor-pointer group">
          <input
            type="checkbox"
            v-model="preferences.uniqueSchedulesOnly"
            class="mt-1 w-4 h-4 text-isu-cardinal border-gray-300 rounded focus:ring-isu-cardinal"
          />
          <div class="flex-1">
            <div class="text-sm font-medium text-gray-700 group-hover:text-isu-cardinal transition-colors">
              <i class="bi bi-filter-circle"></i> Show only unique schedules
            </div>
            <p class="text-xs text-gray-500 mt-1">
              When enabled, hides schedule variations that differ only by location or instructor. Uncheck to see all schedule variations.
            </p>
          </div>
        </label>
      </div>

      <!-- Action Buttons -->
      <div class="flex gap-2 pt-2">
        <button
          @click="applyPreferences"
          class="flex-1 bg-isu-cardinal text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors font-medium"
        >
          <i class="bi bi-check-circle"></i> Apply Preferences
        </button>
        <button
          @click="resetPreferences"
          class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <i class="bi bi-arrow-clockwise"></i> Reset
        </button>
      </div>

      <p class="text-xs text-gray-500 italic">
        <i class="bi bi-info-circle"></i> Preferences are not requirements. Schedules are ordered by match quality, with Schedule 1 being the best match.
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SchedulePreferences',

  emits: ['preferences-changed'],

  data() {
    return {
      expanded: false,
      daysOfWeek: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'],
      preferences: {
        preferredDays: [],
        timePreference: '',
        gapPreference: '',
        scheduleStyle: '',
        uniqueSchedulesOnly: false // Default to false (show all schedules)
      }
    }
  },

  mounted() {
    // Load saved preferences from localStorage
    const saved = localStorage.getItem('schedulePreferences')
    if (saved) {
      try {
        this.preferences = JSON.parse(saved)
      } catch (e) {
        console.error('Failed to load saved preferences:', e)
      }
    }
  },

  methods: {
    applyPreferences() {
      // Save to localStorage
      localStorage.setItem('schedulePreferences', JSON.stringify(this.preferences))

      // Emit preferences to parent
      this.$emit('preferences-changed', this.preferences)

      // Show success message
      this.expanded = false
    },

    resetPreferences() {
      this.preferences = {
        preferredDays: [],
        timePreference: '',
        gapPreference: '',
        scheduleStyle: '',
        uniqueSchedulesOnly: false
      }
      localStorage.removeItem('schedulePreferences')
      this.$emit('preferences-changed', this.preferences)
    },

    expand() {
      this.expanded = true
    }
  }
}
</script>
