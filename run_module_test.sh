#!/bin/bash

# Quick test runner for specific modules
# Usage: ./run_module_test.sh [module_number]

MODULE=$1

if [ -z "$MODULE" ]; then
    echo "Usage: ./run_module_test.sh [module_number]"
    echo "Example: ./run_module_test.sh 1"
    echo ""
    echo "Available modules:"
    echo "  1 - Authentication (Login)"
    echo "  2 - Duty Status"
    echo "  3 - Destination Selection"
    echo "  4 - Dashboard & Boarding"
    echo "  5 - Alighting"
    echo "  6 - Journey History"
    echo "  7 - Settings & Profile"
    echo "  all - Run all tests"
    exit 1
fi

echo "======================================"
echo "Running Module $MODULE Tests"
echo "======================================"
echo ""

case $MODULE in
    1)
        echo "Testing Module 1: Authentication"
        ./gradlew test --tests LoginViewModelTest
        ./gradlew test --tests AuthRepositoryTest
        ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cgana.trmsdriver.LoginActivityTest
        ;;
    2)
        echo "Testing Module 2: Duty Status"
        ./gradlew test --tests DutyStatusViewModelTest
        ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cgana.trmsdriver.DutyStatusActivityTest
        ;;
    3)
        echo "Testing Module 3: Destination Selection"
        ./gradlew test --tests DestinationSelectionViewModelTest
        ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cgana.trmsdriver.DestinationSelectionActivityTest
        ;;
    4)
        echo "Testing Module 4: Dashboard & Boarding"
        ./gradlew test --tests DashboardViewModelTest
        ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cgana.trmsdriver.DashboardBoardingTest
        ;;
    5)
        echo "Testing Module 5: Alighting"
        ./gradlew test --tests AlightingRepositoryTest
        ;;
    6)
        echo "Testing Module 6: Journey History"
        ./gradlew test --tests JourneyHistoryViewModelTest
        ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cgana.trmsdriver.JourneyHistoryActivityTest
        ;;
    7)
        echo "Testing Module 7: Settings & Profile"
        ./gradlew test --tests NetworkUtilsTest
        ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.cgana.trmsdriver.SettingsActivityTest
        ;;
    all)
        echo "Running all tests..."
        ./run_tests.sh
        ;;
    *)
        echo "Invalid module number: $MODULE"
        exit 1
        ;;
esac

echo ""
echo "======================================"
echo "Module $MODULE Tests Completed"
echo "======================================"

