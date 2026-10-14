# Assignment checklist

Source: HappyBirthdayAssignment.pdf. The original PDF is authoritative for assignment wording.

- [x] Stage 1: details screen with app title, name, birthday, picture, and Show birthday screen button. Interpret the button rule as requiring both a nonblank name and a selected birthday.
- [x] Stage 2: edit name, choose date, select gallery photo or take a photo; persist all three across app launches.
- [x] Stage 3: birthday screen matches supplied design/assets; randomly choose among three visual variants on each entry; show completed months below one year, then completed years; wrap long names to two lines; top-left close returns to details.
- [x] Stage 4: camera icon on birthday screen opens gallery/camera choice; chosen photo replaces default picture.
- [ ] Stage 5: Share the news opens share menu with a rendered image excluding share, camera, and close controls.

Develop in order. Detail and completeness matter more than finishing all stages. Use commits and tag each completed stage; Font types may be ignored. Nanit's assets are proprietary and assignment-only.

Clarify edge cases while implementing: future birthdays, newborn zero months, leap-day and month-end birthdays, cancellation and failed camera capture, image persistence, name overflow and small-screen layout. Do not treat proposed policies as PDF requirements.

Status: stages 1–4 are accepted, merged and tagged. Stage 5 is implemented locally; final verification and review are pending.
