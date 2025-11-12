# Contributing to kZones

Thank you for your interest in contributing to kZones! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue with:

1. **Description**: Clear description of the bug
2. **Steps to Reproduce**: Step-by-step instructions
3. **Expected Behavior**: What should happen
4. **Actual Behavior**: What actually happens
5. **Environment**:
   - Minecraft version
   - Server software (Spigot/Paper/etc.)
   - kZones version
   - WorldGuard version
   - Java version
6. **Logs**: Relevant error messages or stack traces

### Suggesting Features

Feature requests are welcome! Please include:

1. **Use Case**: Why this feature would be useful
2. **Description**: Detailed explanation of the feature
3. **Examples**: How it would work in practice
4. **Alternatives**: Any alternative solutions you've considered

### Pull Requests

We love pull requests! Here's how to contribute code:

1. **Fork the Repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/kZones.git
   cd kZones
   ```

2. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Your Changes**
   - Follow the code style (see below)
   - Add comments where necessary
   - Update documentation if needed

4. **Test Your Changes**
   - Build the plugin: `mvn clean package`
   - Test on a local server
   - Verify no existing functionality breaks

5. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "Add: Description of your changes"
   ```

6. **Push to Your Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Open a Pull Request**
   - Go to the original repository
   - Click "New Pull Request"
   - Describe your changes clearly

## Code Style Guidelines

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Braces**: Opening brace on same line
- **Comments**: Use Javadoc for public methods

Example:
```java
public class ExampleClass {
    
    private final String exampleField;
    
    /**
     * Constructor description
     * @param param Parameter description
     */
    public ExampleClass(String param) {
        this.exampleField = param;
    }
    
    /**
     * Method description
     * @return Return value description
     */
    public String getExample() {
        return exampleField;
    }
}
```

### Configuration Files

- **Indentation**: 2 spaces
- **Comments**: Use `#` for comments
- **Keys**: lowercase with hyphens

Example:
```yaml
# Section comment
section-name:
  # Setting comment
  setting-key: value
  another-setting: value
```

## Project Structure

```
kZones/
├── src/main/java/com/kanorto/kzones/
│   ├── KZonesPlugin.java          # Main plugin class
│   ├── commands/                   # Command handlers
│   ├── listeners/                  # Event listeners
│   ├── managers/                   # Business logic managers
│   └── utils/                      # Utility classes
├── src/main/resources/
│   ├── config.yml                  # Default configuration
│   └── plugin.yml                  # Plugin metadata
├── pom.xml                         # Maven configuration
└── README.md                       # Main documentation
```

## Development Setup

1. **Requirements**:
   - Java JDK 21 or higher
   - Maven 3.6 or higher
   - Git
   - IDE (IntelliJ IDEA recommended)

2. **Clone and Build**:
   ```bash
   git clone https://github.com/Kanorto/kZones.git
   cd kZones
   mvn clean install
   ```

3. **Import to IDE**:
   - IntelliJ IDEA: File → Open → Select `pom.xml`
   - Eclipse: File → Import → Existing Maven Project

4. **Testing**:
   - Build: `mvn clean package`
   - Copy JAR from `target/` to test server
   - Test all functionality

## Areas for Contribution

Here are some areas where contributions would be especially valuable:

### Features
- Multiple sequence support per player
- GUI for configuration
- Integration with popular quest plugins
- Database support for player progress
- API for other plugins to interact with kZones

### Improvements
- Performance optimizations
- Better error handling
- More configuration options
- Improved particle effects
- Sound variety

### Documentation
- Video tutorials
- Wiki pages
- More examples
- Translations

### Testing
- Unit tests
- Integration tests
- Performance testing

## Code Review Process

All contributions go through code review:

1. **Automated Checks**: CI will run builds and tests
2. **Code Review**: Maintainers review code quality and style
3. **Testing**: Changes are tested on a server
4. **Merge**: Once approved, changes are merged

## Questions?

If you have questions:

- Open a discussion on GitHub
- Comment on relevant issues
- Reach out to maintainers

## License

By contributing, you agree that your contributions will be licensed under the GPL-3.0 License.

## Thank You!

Your contributions make kZones better for everyone! 🎉
