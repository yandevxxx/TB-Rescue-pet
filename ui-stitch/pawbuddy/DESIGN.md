---
name: PawBuddy
colors:
  surface: '#f4fafd'
  surface-dim: '#d4dbdd'
  surface-bright: '#f4fafd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eef5f7'
  surface-container: '#e8eff1'
  surface-container-high: '#e2e9ec'
  surface-container-highest: '#dde4e6'
  on-surface: '#161d1f'
  on-surface-variant: '#464555'
  inverse-surface: '#2b3234'
  inverse-on-surface: '#ebf2f4'
  outline: '#767586'
  outline-variant: '#c7c4d7'
  surface-tint: '#4849da'
  primary: '#4343d5'
  on-primary: '#ffffff'
  primary-container: '#5d5fef'
  on-primary-container: '#faf7ff'
  inverse-primary: '#c1c1ff'
  secondary: '#8f4e00'
  on-secondary: '#ffffff'
  secondary-container: '#fc9d41'
  on-secondary-container: '#6b3900'
  tertiary: '#006656'
  on-tertiary: '#ffffff'
  tertiary-container: '#00816e'
  on-tertiary-container: '#e1fff5'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e1e0ff'
  primary-fixed-dim: '#c1c1ff'
  on-primary-fixed: '#07006c'
  on-primary-fixed-variant: '#2e2bc2'
  secondary-fixed: '#ffdcc2'
  secondary-fixed-dim: '#ffb77a'
  on-secondary-fixed: '#2e1500'
  on-secondary-fixed-variant: '#6d3a00'
  tertiary-fixed: '#7cf8dd'
  tertiary-fixed-dim: '#5ddbc1'
  on-tertiary-fixed: '#00201a'
  on-tertiary-fixed-variant: '#005144'
  background: '#f4fafd'
  on-background: '#161d1f'
  surface-variant: '#dde4e6'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 48px
  xl: 80px
  gutter: 20px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style
The design system is crafted for a friendly, optimistic, and community-driven pet care ecosystem. It targets pet owners who seek a balance between professional reliability and a warm, approachable atmosphere. The visual direction is **Modern-Friendly**, blending clean structural layouts with soft, organic touches to evoke a sense of trust and comfort.

The style leans into a refined version of minimalism—heavy on whitespace to reduce cognitive load—complemented by subtle "Tactile" elements. Interaction points should feel soft and responsive, mimicking the gentle nature of pet companionship.

## Colors
The palette is centered around a trustworthy primary violet, balanced by an energetic secondary orange that highlights calls to action and playful moments. 

- **Primary:** Used for brand presence, primary actions, and active states.
- **Secondary:** Reserved for high-contrast highlights, notifications, and "joy" moments.
- **Tertiary:** Applied to health-related indicators or success states.
- **Neutral:** A deep charcoal used for high-readability text, ensuring contrast ratios exceed WCAG AA standards.
- **Backgrounds:** Use a very light gray (#F9FAFB) for the base, with pure white (#FFFFFF) reserved for elevated cards and containers.

## Typography
This design system utilizes a dual-font strategy to balance character with readability. **Plus Jakarta Sans** provides a friendly, geometric personality for headlines, featuring soft curves that align with the brand’s approachable nature. **Be Vietnam Pro** is used for all body text and labels, offering exceptional legibility and a contemporary, professional feel.

Scale headlines down appropriately for mobile devices to maintain a clear hierarchy without overwhelming the viewport. Use tighter letter-spacing on larger headlines to ensure a cohesive, modern look.

## Layout & Spacing
The layout follows a **Fluid Grid** model based on an 8px spacing rhythm. 

- **Desktop:** 12-column grid with 32px side margins and 20px gutters.
- **Tablet:** 8-column grid with 24px side margins.
- **Mobile:** 4-column grid with 16px side margins.

Content should be grouped using logical spacing increments (e.g., 24px between related items, 48px between distinct sections). Use the `xl` spacing for large section breaks to maintain the minimalist, airy aesthetic.

## Elevation & Depth
Depth is communicated through **Tonal Layers** and **Ambient Shadows**. This design system avoids harsh borders in favor of soft shadows that suggest physical lift.

- **Level 0 (Base):** Background surface.
- **Level 1 (Cards/Inputs):** White surface with a very soft, diffused shadow (10% opacity) and a 1px neutral-tinted border (#E5E7EB).
- **Level 2 (Modals/Popovers):** Higher lift with a larger blur radius (24px) and 15% shadow opacity to indicate priority.

Background blurs (Glassmorphism) may be used sparingly on navigation bars to maintain context while scrolling.

## Shapes
The shape language is consistently **Rounded**. This reinforces the friendly and safe emotional response required for a pet-focused product.

- **Standard (0.5rem):** Used for buttons, input fields, and small cards.
- **Large (1rem):** Used for main content containers and featured sections.
- **Extra Large (1.5rem):** Used for promotional banners or decorative image masks.
- **Full (Pill):** Used for tags, chips, and specific navigation elements.

## Components
- **Buttons:** Primary buttons use the primary color with white text and a `rounded` (0.5rem) corner radius. Hover states should darken the background slightly.
- **Chips:** Use the `pill` shape with light primary or secondary backgrounds and high-contrast text for categories or pet traits.
- **Cards:** White backgrounds, Level 1 elevation, and `rounded-lg` corners. Padding should be a minimum of 24px.
- **Input Fields:** 1px soft gray border, 16px horizontal padding, and `rounded` corners. Focus states should use a 2px primary color ring.
- **Lists:** Use subtle dividers (1px, #F3F4F6) or tonal cards to separate pet records or activity logs.
- **Selection Controls:** Checkboxes and radio buttons should feel "bouncy" with a 200ms transition and use the primary color for selected states.
- **Pet Avatars:** Always use a circular mask or a very high `rounded-xl` corner to maintain the soft visual theme.