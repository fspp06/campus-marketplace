## 2026-10-20 - Java Swing Forms Accessibility Insight
**Learning:** In Java Swing, simply placing a label next to an input does not associate them for screen readers. Using `JLabel.setLabelFor(Component)` provides the semantic equivalent of HTML's `<label for="...">`.
**Action:** When working on Java Swing interfaces, always ensure form labels use `setLabelFor()` rather than relying entirely on visual layout for context.
