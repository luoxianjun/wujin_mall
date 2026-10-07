---
name: Industrial Precision
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#504532'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
  outline: '#827660'
  outline-variant: '#d4c5ac'
  surface-tint: '#795900'
  primary: '#795900'
  on-primary: '#ffffff'
  primary-container: '#f6b800'
  on-primary-container: '#674b00'
  inverse-primary: '#fbbc0c'
  secondary: '#575e70'
  on-secondary: '#ffffff'
  secondary-container: '#d9dff5'
  on-secondary-container: '#5c6274'
  tertiary: '#006d3c'
  on-tertiary: '#ffffff'
  tertiary-container: '#4bdb8a'
  on-tertiary-container: '#005c32'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdea0'
  primary-fixed-dim: '#fbbc0c'
  on-primary-fixed: '#261a00'
  on-primary-fixed-variant: '#5c4300'
  secondary-fixed: '#dce2f7'
  secondary-fixed-dim: '#c0c6db'
  on-secondary-fixed: '#141b2b'
  on-secondary-fixed-variant: '#404758'
  tertiary-fixed: '#70fda7'
  tertiary-fixed-dim: '#51df8e'
  on-tertiary-fixed: '#00210e'
  on-tertiary-fixed-variant: '#00522c'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  headline-xl:
    fontFamily: Hanken Grotesk
    fontSize: 42px
    fontWeight: '700'
    lineHeight: 52px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Hanken Grotesk
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  container-margin: 16px
  gutter: 12px
  card-padding: 16px
  stack-gap: 8px
  section-gap: 24px
---

## Brand & Style

The design system is engineered for a B2B industrial hardware environment where efficiency, reliability, and technical clarity are paramount. The aesthetic follows a **Light Industrial / Modern Corporate** hybrid, moving away from cluttered "warehouse" vibes toward a precise, tool-like interface.

The experience is centered on "Utility-First" design:
- **Professionalism:** High-contrast typography and structured information density reflect the seriousness of procurement.
- **Efficiency:** Functional color usage (Yellow for primary actions) guides the eye to conversion points instantly.
- **Reliability:** Heavy use of borders and distinct surfaces creates a robust, physical feel, echoing the hardware products it showcases.
- **Clarity:** Ample whitespace and a cool-toned neutral palette ensure that complex technical specifications are legible and easy to compare.

## Colors

This color palette is designed for high-visibility industrial tasks. 

- **Primary (Action Yellow):** Reserved strictly for primary call-to-actions, active navigation states, and critical highlights like price tags and ratings. It draws inspiration from construction safety and industrial signage.
- **Secondary (Deep Charcoal):** Used for structural elements and core branding to provide a grounded, authoritative contrast.
- **Success (Industrial Green):** Indicates inventory availability, completed transactions, and verified supplier statuses.
- **Neutrals:** The background (#F8FAFC) provides a crisp, cool foundation that allows the white surfaces (#FFFFFF) to pop, creating a clear "layered" hierarchy.
- **Borders:** A consistent light gray (#E5E7EB) defines the grid and separates technical data points without adding visual noise.

## Typography

The typography system uses **Hanken Grotesk** for its sharp, contemporary, and technical appearance. It balances the "grotesque" industrial heritage with modern readability. 

- **Headlines:** Use Bold (700) and Semibold (600) weights to establish a clear hierarchy in product names and section titles.
- **Technical Specs:** Body-sm and Label-md are optimized for dense data tables, supplier information, and SKU details.
- **Mobile Scaling:** Headlines above 32px should scale down to 24px-28px on mobile devices to maintain structural integrity.
- **Case Usage:** Labels for technical attributes (e.g., "MOQ", "SKU") should use Inter with a slight letter-spacing increase to improve recognition in data-heavy views.

## Layout & Spacing

The layout follows a **Fixed-Width / Container-based** approach for mobile-first procurement, ensuring that product lists and specifications remain consistently aligned across different handsets.

- **Grid:** A 4px baseline grid governs all spacing. 
- **Margins:** 16px (container-margin) is the standard safe area for all mobile screens.
- **Rhythm:** Use 8px (stack-gap) for related items within a card (e.g., title to price) and 24px (section-gap) to separate distinct functional blocks on a page.
- **Density:** The layout is "Efficient," meaning padding is tight enough to show multiple product listings at once but generous enough to maintain a premium, organized feel.

## Elevation & Depth

Visual hierarchy is achieved through a combination of **Tonal Layering** and **Subtle Shadows**. 

- **Surface Levels:** The background (#F8FAFC) is the lowest level. Active content sits on white cards (#FFFFFF).
- **Shadows:** We use a single, purposeful shadow style: `0 8px 24px rgba(17, 24, 39, 0.06)`. This is a soft, diffused charcoal shadow that provides lift without making the UI feel "floaty."
- **Interaction Depth:** Elements like primary buttons or active chips do not use heavy gradients; instead, they rely on color fills and 1px borders to denote state.
- **Outlines:** A 1px border (#E5E7EB) is the primary method for defining card boundaries and input fields, reinforcing the industrial, structured nature of the system.

## Shapes

The shape language is **Soft-Geometric**. While industrial tools are often sharp, the UI uses rounded corners to improve approachability and ergonomics in digital interactions.

- **Standard Radius:** 8px (0.5rem) for primary buttons and small input fields.
- **Container Radius:** 16px (1rem / `rounded-lg`) for all product cards and modal sheets.
- **Pill Shapes:** Reserved for "Status Chips" (e.g., "In Stock", "Verified") to distinguish them from interactive buttons.
- **Iconography:** Icons should be linear, 2px stroke weight, with slightly rounded terminal ends to match the UI's radius.

## Components

### Buttons
- **Primary:** Action Yellow (#F6B800) background, Charcoal (#111827) text. Bold weight.
- **Secondary:** White background, 1px border (#E5E7EB), Charcoal text.
- **Ghost:** No background, Secondary Text (#475467). Used for secondary actions like "View More".

### Cards
- **Product Card:** White surface, 16px radius, 16px padding. Includes the 8px/24px/0.06 shadow.
- **Supplier Card:** Uses a light gray subtle background or border to differentiate from product listings.

### Inputs & Selection
- **Fields:** 8px radius, 1px border (#E5E7EB). Active state uses a 1px Primary Yellow border.
- **Checkboxes/Radios:** Rounded-sm (4px), using Primary Yellow for the checked state.

### Chips & Tags
- **Technical Tags:** Small text, light gray background, used for material types (e.g., "Stainless Steel").
- **Status Tags:** Pill-shaped. Success Green (#12B76A) with 10% opacity background for "Available" or "Certified."

### Lists
- Standardized vertical lists for specifications. Each row is separated by a 1px horizontal rule (#E5E7EB) with consistent 12px vertical padding.