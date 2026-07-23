# Task List - Workout Completion & Badges

## Phase 1: Data Layer Refinement
- [x] Add `weight` and `supplements` to `WorkoutSession` entity
- [x] Increment `AppDatabase` version to 5 (destructive migration)

## Phase 2: Completion Flow (Notification & Service)
- [x] Update `WorkoutTimerService` notification action to target `MainActivity`
- [x] Implement `ACTION_END_WORKOUT` handling in `MainActivity`
- [x] Implement `WorkoutCompletionDialog` in `MainActivity` (or Home)

## Phase 3: ViewModel Logic
- [x] Add `saveWorkoutSession` method to `FitnessViewModel`
- [x] Implement `isTodayCompleted` flow in `FitnessViewModel`

## Phase 4: UI Updates
- [x] Add "Completed" badge to `HomeScreen` today's workout card
- [x] Verify that stopping from app also triggers the completion dialog
