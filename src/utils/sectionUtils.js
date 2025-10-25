/**
 * Utility functions for determining section types and categories.
 * Provides a single source of truth for section classification throughout the frontend.
 */

/**
 * Determines if a section is an online section based on multiple indicators.
 * @param {Object} section - The section object to check
 * @returns {boolean} True if the section is online
 */
export function isOnlineSection(section) {
  if (!section) return false

  return (
    section.deliveryMode === 'Online' ||
    section.daysOfTheWeek === 'Online' ||
    section.timeStart === 'Online' ||
    section.location === 'Online' ||
    (section.daysOfTheWeek === 'N/A' && section.deliveryMode === 'Online')
  )
}

/**
 * Determines if a section has TBD (To Be Determined) meeting times or days.
 * @param {Object} section - The section object to check
 * @returns {boolean} True if the section has TBD status
 */
export function isTBDSection(section) {
  if (!section) return false

  return (
    section.daysOfTheWeek === 'TBD' ||
    section.timeStart === 'TBD' ||
    section.timeEnd === 'TBD'
  )
}

/**
 * Determines if a section is an in-person section with valid scheduling information.
 * @param {Object} section - The section object to check
 * @returns {boolean} True if the section is in-person and schedulable
 */
export function isInPersonSection(section) {
  if (!section) return false

  // Not online or TBD, and has valid day/time information
  return !isOnlineSection(section) &&
         !isTBDSection(section) &&
         section.daysOfTheWeek &&
         section.daysOfTheWeek !== 'N/A' &&
         section.timeStart &&
         section.timeStart !== 'N/A'
}

/**
 * Gets the category of a section for display purposes.
 * @param {Object} section - The section object to categorize
 * @returns {'online'|'tbd'|'in-person'} The section category
 */
export function getSectionCategory(section) {
  if (isOnlineSection(section)) {
    return 'online'
  }
  if (isTBDSection(section)) {
    return 'tbd'
  }
  return 'in-person'
}

/**
 * Checks if a section should be displayed in the calendar grid.
 * Only in-person sections with valid times should appear in the calendar.
 * @param {Object} section - The section object to check
 * @returns {boolean} True if the section should be in the calendar
 */
export function shouldShowInCalendar(section) {
  return isInPersonSection(section)
}
