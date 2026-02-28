#!/bin/bash

TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QGF0b3pzaG9wLmNvbSIsInRlbmFudElkIjoxLCJpYXQiOjE3NzIyNjkwMDIsImV4cCI6MTc3MjM1NTQwMn0.FpynMEztNS-E5vfShg0asNKqmRCWabGXxHVaNwrNuzyb0YxrYMGDXw6icpDyobUEF65EAIAN1SPr1LeNbgvZrg"

# Test authenticated health endpoint
curl -s -X GET http://localhost:8080/api/v1/auth/health \
  -H "Authorization: Bearer $TOKEN"
