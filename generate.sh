#!/bin/bash
#
# nowinandroid-agent-kit — Project Generator
#
# Usage:
#   ./generate.sh --name "MyApp" --package "com.example.myapp" --output ~/Projects/MyApp
#
# This generates a complete multi-module Android project based on NowInAndroid architecture,
# with AI agent rules, skills, and agents pre-configured.

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

print_step() { echo -e "${BLUE}==>${NC} ${BOLD}$1${NC}"; }
print_done() { echo -e "${GREEN}✓${NC} $1"; }
print_error() { echo -e "${RED}✗${NC} $1"; exit 1; }

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --name) PROJECT_NAME="$2"; shift 2 ;;
        --package) PACKAGE_NAME="$2"; shift 2 ;;
        --output) OUTPUT_DIR="$2"; shift 2 ;;
        -h|--help)
            echo "Usage: ./generate.sh --name \"MyApp\" --package \"com.example.myapp\" --output ~/Projects/MyApp"
            echo ""
            echo "Options:"
            echo "  --name     Project name (e.g., MyApp)"
            echo "  --package  Package name (e.g., com.example.myapp)"
            echo "  --output   Output directory"
            exit 0 ;;
        *) print_error "Unknown option: $1" ;;
    esac
done

# Interactive prompts if not provided
if [ -z "$PROJECT_NAME" ]; then
    read -p "Project name (e.g., MyApp): " PROJECT_NAME
fi
if [ -z "$PACKAGE_NAME" ]; then
    read -p "Package name (e.g., com.example.myapp): " PACKAGE_NAME
fi
if [ -z "$OUTPUT_DIR" ]; then
    read -p "Output directory (e.g., ~/Projects/MyApp): " OUTPUT_DIR
fi

# Expand ~ in OUTPUT_DIR
OUTPUT_DIR="${OUTPUT_DIR/#\~/$HOME}"

# Validate
[ -z "$PROJECT_NAME" ] && print_error "Project name is required"
[ -z "$PACKAGE_NAME" ] && print_error "Package name is required"
[ -z "$OUTPUT_DIR" ] && print_error "Output directory is required"

# Get script directory (where scaffold/ lives)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SCAFFOLD_DIR="$SCRIPT_DIR/scaffold"

if [ ! -d "$SCAFFOLD_DIR" ]; then
    print_error "scaffold/ directory not found at $SCAFFOLD_DIR"
fi

echo ""
echo -e "${BOLD}nowinandroid-agent-kit${NC} — Project Generator"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Project:  $PROJECT_NAME"
echo "  Package:  $PACKAGE_NAME"
echo "  Output:   $OUTPUT_DIR"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Convert package to path (com.example.myapp → com/example/myapp)
PACKAGE_PATH="${PACKAGE_NAME//./\/}"

# Step 1: Copy scaffold
print_step "Copying project scaffold..."
mkdir -p "$OUTPUT_DIR"
cp -r "$SCAFFOLD_DIR"/* "$OUTPUT_DIR/"
cp "$SCAFFOLD_DIR"/.* "$OUTPUT_DIR/" 2>/dev/null || true
print_done "Scaffold copied"

# Step 2: Replace placeholders in all files
print_step "Replacing placeholders..."
find "$OUTPUT_DIR" -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.template" -o -name "*.toml" -o -name "*.md" \) | while read -r file; do
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" "$file"
        sed -i '' "s/{{PACKAGE_NAME}}/$PACKAGE_NAME/g" "$file"
    else
        sed -i "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" "$file"
        sed -i "s/{{PACKAGE_NAME}}/$PACKAGE_NAME/g" "$file"
    fi
done
print_done "Placeholders replaced"

# Step 3: Move Kotlin source files to correct package directories
print_step "Organizing source files into package directories..."

organize_kotlin_sources() {
    local base_dir="$1"
    if [ -d "$base_dir" ]; then
        # Find all .kt files directly in the kotlin/ directory (not in subdirectories with packages)
        find "$base_dir" -maxdepth 1 -name "*.kt" | while read -r kt_file; do
            # Extract package from file
            local pkg
            pkg=$(grep -m1 "^package " "$kt_file" | sed 's/package //' | tr '.' '/' | tr -d '\r')
            if [ -n "$pkg" ]; then
                local target_dir="$base_dir/$pkg"
                mkdir -p "$target_dir"
                mv "$kt_file" "$target_dir/"
            fi
        done
        # Also handle files in model/ dao/ etc subdirs
        find "$base_dir" -mindepth 2 -name "*.kt" | while read -r kt_file; do
            local parent_dir
            parent_dir=$(dirname "$kt_file")
            local parent_name
            parent_name=$(basename "$parent_dir")
            # Skip if already in a deep package path
            local depth
            depth=$(echo "$parent_dir" | tr '/' '\n' | wc -l)
            local base_depth
            base_depth=$(echo "$base_dir" | tr '/' '\n' | wc -l)
            if [ "$((depth - base_depth))" -le 2 ]; then
                local pkg
                pkg=$(grep -m1 "^package " "$kt_file" | sed 's/package //' | tr '.' '/' | tr -d '\r')
                if [ -n "$pkg" ]; then
                    local target_dir="$base_dir/$pkg"
                    mkdir -p "$target_dir"
                    mv "$kt_file" "$target_dir/"
                fi
            fi
        done
        # Clean up empty directories
        find "$base_dir" -type d -empty -delete 2>/dev/null || true
    fi
}

# Organize all kotlin source directories
find "$OUTPUT_DIR" -type d -name "kotlin" | while read -r kotlin_dir; do
    organize_kotlin_sources "$kotlin_dir"
done
print_done "Sources organized"

# Step 4: Copy AI agent files
print_step "Setting up AI agent harness (.claude/)..."
mkdir -p "$OUTPUT_DIR/.claude/rules" "$OUTPUT_DIR/.claude/skills" "$OUTPUT_DIR/.claude/agents"
cp -r "$SCRIPT_DIR/rules"/* "$OUTPUT_DIR/.claude/rules/"
cp -r "$SCRIPT_DIR/skills"/* "$OUTPUT_DIR/.claude/skills/"
cp -r "$SCRIPT_DIR/agents"/* "$OUTPUT_DIR/.claude/agents/"

# Generate CLAUDE.md from template
if [ -f "$SCRIPT_DIR/templates/CLAUDE.md.template" ]; then
    cp "$SCRIPT_DIR/templates/CLAUDE.md.template" "$OUTPUT_DIR/CLAUDE.md"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" "$OUTPUT_DIR/CLAUDE.md"
        sed -i '' "s/{{PACKAGE_NAME}}/$PACKAGE_NAME/g" "$OUTPUT_DIR/CLAUDE.md"
    else
        sed -i "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" "$OUTPUT_DIR/CLAUDE.md"
        sed -i "s/{{PACKAGE_NAME}}/$PACKAGE_NAME/g" "$OUTPUT_DIR/CLAUDE.md"
    fi
fi
print_done "AI harness configured"

# Step 5: Initialize git
print_step "Initializing git repository..."
cd "$OUTPUT_DIR"
git init -q
git add -A
git commit -q -m "Initial project from nowinandroid-agent-kit"
print_done "Git initialized"

# Step 6: Add gradle wrapper placeholder
print_step "Creating gradle wrapper placeholder..."
mkdir -p "$OUTPUT_DIR/gradle/wrapper"
cat > "$OUTPUT_DIR/gradle.properties" << 'PROPS'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
PROPS
print_done "Gradle properties created"

# Done
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}${BOLD}✓ Project generated successfully!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  cd $OUTPUT_DIR"
echo ""
echo "  Project structure:"
echo "  ├── app/                    (Entry point + navigation)"
echo "  ├── core/"
echo "  │   ├── model/             (Domain data classes)"
echo "  │   ├── domain/            (UseCases)"
echo "  │   ├── data/              (Repository impl)"
echo "  │   ├── database/          (Room DAOs + Entities)"
echo "  │   ├── network/           (Retrofit + OkHttp)"
echo "  │   ├── designsystem/      (Theme + Components)"
echo "  │   └── ...                (navigation, ui, common, testing)"
echo "  ├── feature/"
echo "  │   ├── home/              (Sample feature — copy this for new features)"
echo "  │   └── settings/"
echo "  ├── .claude/               (AI agent rules + skills + agents)"
echo "  └── CLAUDE.md"
echo ""
echo "  Next steps:"
echo "  1. Open in Android Studio"
echo "  2. Replace BASE_URL in core/network/NetworkModule.kt"
echo "  3. Add your features by copying feature/home/ pattern"
echo "  4. Run: claude (AI agent with full NIA context)"
echo ""
