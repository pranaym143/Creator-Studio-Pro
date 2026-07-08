/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        theme: {
          bg: 'var(--bg-color)',
          surface: 'var(--surface-color)',
          hover: 'var(--surface-hover)',
          border: 'var(--border-color)',
          primary: 'var(--primary-color)',
          secondary: 'var(--secondary-color)',
          accent: 'var(--accent-color)',
          success: 'var(--success-color)',
          warning: 'var(--warning-color)',
          error: 'var(--error-color)',
          text: 'var(--text-color)',
          textSecondary: 'var(--text-secondary)',
        }
      },
      fontFamily: {
        mono: ['var(--font-mono)', 'monospace'],
      }
    },
  },
  plugins: [],
}
