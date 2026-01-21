#!/bin/bash

# TRMS Driver App - Automated Test Suite Runner
# This script runs all unit and instrumented tests

set -e

echo "======================================"
echo "TRMS Driver App - Test Automation"
echo "======================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test results
UNIT_TEST_RESULT=0
INSTRUMENTED_TEST_RESULT=0

echo "Starting test automation suite..."
echo ""

# Function to print colored output
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Clean previous test results
echo "Cleaning previous test results..."
./gradlew clean
echo ""

# Run Unit Tests
echo "======================================"
echo "Running Unit Tests..."
echo "======================================"
./gradlew test --info || UNIT_TEST_RESULT=$?
echo ""

print_status $UNIT_TEST_RESULT "Unit Tests Completed"
echo ""

# Check if connected device/emulator is available
echo "Checking for connected devices..."
DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)

if [ $DEVICE_COUNT -eq 0 ]; then
    echo -e "${YELLOW}Warning: No Android device/emulator detected${NC}"
    echo "Skipping instrumented tests..."
    echo ""
else
    echo "Found $DEVICE_COUNT device(s)"
    echo ""

    # Run Instrumented Tests
    echo "======================================"
    echo "Running Instrumented Tests..."
    echo "======================================"
    ./gradlew connectedAndroidTest --info || INSTRUMENTED_TEST_RESULT=$?
    echo ""

    print_status $INSTRUMENTED_TEST_RESULT "Instrumented Tests Completed"
    echo ""
fi

# Generate Test Reports
echo "======================================"
echo "Generating Test Reports..."
echo "======================================"

# Check if test reports exist
if [ -d "app/build/reports/tests" ]; then
    echo "Unit Test Report: app/build/reports/tests/testDebugUnitTest/index.html"
fi

if [ -d "app/build/reports/androidTests" ]; then
    echo "Instrumented Test Report: app/build/reports/androidTests/connected/index.html"
fi

echo ""

# Print Summary
echo "======================================"
echo "Test Summary"
echo "======================================"
print_status $UNIT_TEST_RESULT "Unit Tests: $UNIT_TEST_RESULT"
print_status $INSTRUMENTED_TEST_RESULT "Instrumented Tests: $INSTRUMENTED_TEST_RESULT"
echo ""

# Overall result
OVERALL_RESULT=$((UNIT_TEST_RESULT + INSTRUMENTED_TEST_RESULT))

if [ $OVERALL_RESULT -eq 0 ]; then
    echo -e "${GREEN}======================================"
    echo "All tests passed successfully!"
    echo "======================================${NC}"
    exit 0
else
    echo -e "${RED}======================================"
    echo "Some tests failed. Check reports."
    echo "======================================${NC}"
    exit 1
fi

