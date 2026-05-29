/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink:   '#111827',
        slate: '#1F2937',
        steel: '#374151',
        ash:   '#6B7280',
        mist:  '#9CA3AF',
        line:  '#D1D5DB',
        soft:  '#E5E7EB',
        wash:  '#F3F4F6',
        cloud: '#F9FAFB',
      },
    },
  },
  plugins: [],
  corePlugins: { preflight: false },
}
