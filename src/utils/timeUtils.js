/**
 * Convert "HH:MM AM/PM" to minutes since midnight
 */
export function timeToMinutes(time) {
  if (!time || time === 'N/A') return 0;

  const parts = time.match(/(\d+):(\d+)\s*(AM|PM)/i);
  if (!parts) return 0;

  let hours = parseInt(parts[1]);
  const minutes = parseInt(parts[2]);
  const period = parts[3].toUpperCase();

  if (period === 'PM' && hours !== 12) hours += 12;
  if (period === 'AM' && hours === 12) hours = 0;

  return hours * 60 + minutes;
}

/**
 * Format minutes since midnight to "HH:MM AM/PM"
 */
export function minutesToTime(minutes) {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  const period = hours >= 12 ? 'PM' : 'AM';
  const displayHours = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;

  return `${displayHours}:${mins.toString().padStart(2, '0')} ${period}`;
}

/**
 * Get day abbreviation from full day name
 */
export function getDayAbbreviation(dayName) {
  const dayMap = {
    'Monday': 'Mon',
    'Tuesday': 'Tue',
    'Wednesday': 'Wed',
    'Thursday': 'Thu',
    'Friday': 'Fri',
    'Saturday': 'Sat',
    'Sunday': 'Sun'
  };
  return dayMap[dayName] || dayName;
}

/**
 * Get full day name from abbreviation
 */
export function getFullDayName(abbreviation) {
  const dayMap = {
    'Mon': 'Monday',
    'Tue': 'Tuesday',
    'Wed': 'Wednesday',
    'Thu': 'Thursday',
    'Fri': 'Friday',
    'Sat': 'Saturday',
    'Sun': 'Sunday'
  };
  return dayMap[abbreviation] || abbreviation;
}
