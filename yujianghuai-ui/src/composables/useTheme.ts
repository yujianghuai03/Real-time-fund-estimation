import { computed, ref } from 'vue'

export type ThemeName = 'dark-blue' | 'light' | 'oled-black'

interface ThemeOption {
  label: string
  value: ThemeName
}

const THEME_STORAGE_KEY = 'theme'

export const themeOptions: ThemeOption[] = [
  { label: '深色科技蓝', value: 'dark-blue' },
  { label: '浅色简约', value: 'light' },
  { label: '暗黑纯黑', value: 'oled-black' },
]

const getInitialTheme = (): ThemeName => {
  if (typeof window === 'undefined') {
    return 'dark-blue'
  }

  const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY)
  return themeOptions.some((theme) => theme.value === storedTheme) ? (storedTheme as ThemeName) : 'dark-blue'
}

const currentTheme = ref<ThemeName>(getInitialTheme())

const applyTheme = (theme: ThemeName): void => {
  if (typeof document === 'undefined') {
    return
  }

  document.documentElement.dataset.theme = theme
  document.documentElement.style.colorScheme = theme === 'light' ? 'light' : 'dark'
}

applyTheme(currentTheme.value)

export const useTheme = () => {
  const currentThemeLabel = computed(() => {
    return themeOptions.find((theme) => theme.value === currentTheme.value)?.label ?? themeOptions[0].label
  })

  const isDarkTheme = computed(() => currentTheme.value !== 'light')

  const setTheme = (theme: ThemeName): void => {
    currentTheme.value = theme
    window.localStorage.setItem(THEME_STORAGE_KEY, theme)
    applyTheme(theme)
  }

  const toggleTheme = (): void => {
    const currentIndex = themeOptions.findIndex((theme) => theme.value === currentTheme.value)
    const nextTheme = themeOptions[(currentIndex + 1) % themeOptions.length]
    setTheme(nextTheme.value)
  }

  return {
    currentTheme,
    currentThemeLabel,
    isDarkTheme,
    setTheme,
    themeOptions,
    toggleTheme,
  }
}
