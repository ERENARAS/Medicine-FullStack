
import uuid
import random
import csv
from datetime import datetime, timedelta

# Configuration
CSV_FILE = "medicine_treatment_simple_500.csv"
SQL_FILE = "seed.sql"
YEARS = 2
START_DATE = datetime.now() - timedelta(days=YEARS * 365)
NAMESPACE = uuid.NAMESPACE_DNS  # Deterministic UUID namespace

# Data containers
medicines = [] # List of tuples/dicts: (id, name, treatment)

# 1. Read Medicines from CSV
print(f"Reading medicines from {CSV_FILE}...")
try:
    with open(CSV_FILE, mode='r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row.get('medicine_name', '').strip()
            treatment = row.get('treatment', '').strip()
            
            if name:
                # Deterministic UUID generation
                # Using the name as the seed ensures the same UUID every time
                med_id = str(uuid.uuid5(NAMESPACE, name))
                medicines.append({'id': med_id, 'name': name, 'treatment': treatment})
                
    print(f"Loaded {len(medicines)} medicines.")

except FileNotFoundError:
    print(f"Error: {CSV_FILE} not found. Please ensure the file is in the current directory.")
    exit(1)

# Fixed IDs for Doctor and Patient (Long/BigInt compatible)
doctor_id = 102
patient_id = 102

sql_statements = []

# Optional: Truncate tables to start fresh (Be careful with this in prod, but ok for seed)
# sql_statements.append("TRUNCATE TABLE prescription_medicine, prescription, medicine CASCADE;")

# 2. Insert Doctor and Patients
# Doctor: id, name, email, password
sql_statements.append(f"INSERT INTO doctor (id, name, email, password) VALUES ({doctor_id}, 'Dr. Eren Aras', 'doctoreren@dr.medicine', '123') ON CONFLICT (id) DO NOTHING;")

# Patient: id, name, email, password, amount
sql_statements.append(f"INSERT INTO patient (id, name, email, password, amount) VALUES ({patient_id}, 'Enes Aras', 'enes@pt.medicine', '123', 0.0) ON CONFLICT (id) DO NOTHING;")
sql_statements.append(f"INSERT INTO patient (id, name, email, password, amount) VALUES ({patient_id+1}, 'Can Yilmaz', 'can@pt.medicine', '123', 0.0) ON CONFLICT (id) DO NOTHING;")
sql_statements.append(f"INSERT INTO patient (id, name, email, password, amount) VALUES ({patient_id+2}, 'Mustafa Demir', 'mustafa@pt.medicine', '123', 0.0) ON CONFLICT (id) DO NOTHING;")
sql_statements.append(f"INSERT INTO patient (id, name, email, password, amount) VALUES ({patient_id+3}, 'Zeynep Gonul', 'zeynep@pt.medicine', '123', 0.0) ON CONFLICT (id) DO NOTHING;")

# 3. Insert Medicines
# Medicine: id(UUID), name, treatment
for med in medicines:
    safe_name = med['name'].replace("'", "''")
    safe_treatment = med['treatment'].replace("'", "''")
    
    # Using ON CONFLICT DO NOTHING to avoid duplicate key errors if re-run
    # Since UUID is deterministic based on name, this safely handles re-seeding
    sql_statements.append(f"INSERT INTO medicine (id, name, treatment) VALUES ('{med['id']}', '{safe_name}', '{safe_treatment}') ON CONFLICT DO NOTHING;")

# 4. Generate Prescriptions
current_date = START_DATE
end_date = datetime.now()

prescriptions_count = 0

POPULAR_MEDICINES = ["Saizen", "Iliadin", "Lansor", "Glukofen", "Arveles", "Diclomec", "Parol", "Majezik", "Aspirin"]

# Separate medicines into two groups
popular_med_objs = [m for m in medicines if m['name'] in POPULAR_MEDICINES]
other_med_objs = [m for m in medicines if m['name'] not in POPULAR_MEDICINES]

print(f"Found {len(popular_med_objs)} popular medicines and {len(other_med_objs)} other medicines.")

while current_date < end_date:
    # Randomly decide how many prescriptions per day (0 to 15)
    daily_count = random.randint(0, 15)
    
    for _ in range(daily_count):
        prescription_id = str(uuid.uuid4()) # Prescriptions still random
        
        # Determine how many items in this prescription (1 to 3)
        num_items = random.randint(1, 3)
        selected_meds = []
        
        for _ in range(num_items):
            # Weighted selection: 80% chance for popular medicines
            if popular_med_objs and random.random() < 0.8:
                selected_meds.append(random.choice(popular_med_objs))
            elif other_med_objs:
                selected_meds.append(random.choice(other_med_objs))
            elif popular_med_objs: # Fallback if no others
                selected_meds.append(random.choice(popular_med_objs))
        
        # Deduplicate items in single prescription
        # (use id as key to deduplicate)
        unique_selected_meds = {m['id']: m for m in selected_meds}.values()

        # Rotate patients for variety
        change_patient_int = random.randint(0, 3)
        current_patient_id = patient_id + change_patient_int
        
        # Insert Prescription
        sql_statements.append(
            f"INSERT INTO prescription (id, date, dispensed, doctor_id, patient_id) "
            f"VALUES ('{prescription_id}', '{current_date.strftime('%Y-%m-%d')}', true, {doctor_id}, {current_patient_id});"
        )
        
        # Insert items into join table
        for med in unique_selected_meds:
            sql_statements.append(
                f"INSERT INTO prescription_medicine (prescription_id, medicine_id) "
                f"VALUES ('{prescription_id}', '{med['id']}');"
            )
        prescriptions_count += 1
            
    current_date += timedelta(days=1)

# Write to file
with open(SQL_FILE, "w", encoding='utf-8') as f:
    f.write("\n".join(sql_statements))

print(f"Generated {len(sql_statements)} SQL statements (for {len(medicines)} medicines and {prescriptions_count} prescriptions) in {SQL_FILE}")
