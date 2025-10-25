<template>
  <transition name="fade">
    <div
      v-if="message"
      :class="[
        'fixed top-4 right-4 z-[100] px-6 py-4 rounded-lg shadow-lg text-white max-w-md',
        typeClasses
      ]"
      role="alert"
    >
      <div class="flex items-center gap-3">
        <i :class="iconClass"></i>
        <span>{{ message }}</span>
        <button
          v-if="dismissible"
          @click="dismiss"
          class="ml-auto text-white hover:text-gray-200 transition-colors"
          aria-label="Dismiss message"
        >
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'FlashMessage',

  props: {
    message: {
      type: String,
      default: ''
    },
    type: {
      type: String,
      default: 'info',
      validator: (value) => ['success', 'danger', 'warning', 'info'].includes(value)
    },
    dismissible: {
      type: Boolean,
      default: true
    },
    duration: {
      type: Number,
      default: 3000
    }
  },

  emits: ['dismiss'],

  data() {
    return {
      timeoutId: null
    }
  },

  computed: {
    typeClasses() {
      const classes = {
        success: 'bg-green-500',
        danger: 'bg-red-500',
        warning: 'bg-yellow-500',
        info: 'bg-blue-500'
      }
      return classes[this.type] || classes.info
    },

    iconClass() {
      const icons = {
        success: 'bi bi-check-circle',
        danger: 'bi bi-exclamation-triangle',
        warning: 'bi bi-exclamation-circle',
        info: 'bi bi-info-circle'
      }
      return icons[this.type] || icons.info
    }
  },

  watch: {
    message(newVal) {
      if (newVal && this.duration > 0) {
        this.startTimer()
      }
    }
  },

  mounted() {
    if (this.message && this.duration > 0) {
      this.startTimer()
    }
  },

  beforeUnmount() {
    this.clearTimer()
  },

  methods: {
    startTimer() {
      this.clearTimer()
      this.timeoutId = setTimeout(() => {
        this.dismiss()
      }, this.duration)
    },

    clearTimer() {
      if (this.timeoutId) {
        clearTimeout(this.timeoutId)
        this.timeoutId = null
      }
    },

    dismiss() {
      this.clearTimer()
      this.$emit('dismiss')
    }
  }
}
</script>
