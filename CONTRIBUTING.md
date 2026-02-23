# Contributing to Retry Library

Thank you for your interest in contributing to **Retry Library** 🚀  
This document describes the contribution workflow and expectations.  
For general information about features, usage, installation, or project structure, please see the `README.md`.

---

## 🎯 Contribution Principles

Retry Library aims to remain:

- Small and focused
- Dependency-free
- Clean and readable
- Predictable in behavior
- Backward-compatible

When contributing, please ensure your changes respect these goals.

---

## 🧭 Before You Start

### 1️⃣ Search Existing Issues

Before creating a new issue or PR:

- Check whether the problem or feature request already exists.
- If it does, join the discussion instead of duplicating it.

### 2️⃣ Discuss Larger Changes First

If you are planning:

- API changes
- New retry strategies (e.g., exponential backoff variants)
- Behavioral changes
- Breaking changes

Please open an issue first to discuss the design.

---

## 🛠 Development Guidelines

### Code Style

- Follow standard Java conventions.
- Prefer immutability where possible.
- Keep methods small and focused.
- Avoid unnecessary abstraction.
- Avoid adding external dependencies.
- Write clear Javadoc for public APIs.

### API Design Rules

- Public APIs must remain simple and fluent.
- Avoid framework-style complexity.
- Maintain backward compatibility whenever possible.
- If deprecation is necessary, mark it clearly and explain why.

---

## 🧪 Testing Requirements

All contributions must include appropriate tests.

Specifically:

- New features must include unit tests.
- Bug fixes must include regression tests.
- Edge cases must be covered (e.g., max attempts, retry predicates, listener order).
- Tests must be deterministic (no uncontrolled timing behavior).

Pull requests without tests will not be accepted.

---

## 🔄 Pull Request Process

### 1️⃣ Fork and Create Branch

Create a feature branch from `main`:

```bash
git checkout -b feature/short-description
```

### 2️⃣ Commit Message Rules

Use clear, concise messages in present tense:

- ✅ Add support for custom delay strategy
- ❌ Added support for custom delay strategy

Keep commits logically grouped.

### 3️⃣ Pull Request Checklist

Before submitting:

- Project builds successfully
- All tests pass
- Tests added for new functionality
- Public APIs documented
- No unrelated formatting changes
- No unnecessary dependency introduced

In your PR description, explain:
- What problem is solved
- Why the change is needed
- Any design trade-offs

---

## 🐞 Reporting Bugs

When opening a bug report, please include:
- Java version
- Library version
- Minimal reproducible example
- Expected behavior
- Actual behavior
- Stack trace (if available)

Clear reports help resolve issues faster.

---

## 📜 License

By contributing, you agree that your contributions will be licensed under the same license as the project (Apache 2.0).

---

Thank you for helping improve Retry Library! 🙌
