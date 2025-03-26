import sys
import tkinter
from tkinter import filedialog
def openFile():
    tkinter.Tk().withdraw() # prevents an empty tkinter window from appearing
    print(filedialog.askopenfilename(filetypes=[("All accepted types",".txt .csv .xlsx .xls"),("text file", "*.txt"),("CSV file", "*.csv"), ("Excel files", ".xlsx .xls")]))
if __name__ == "__main__":
    globals()[sys.argv[1]]()
    sys.exit()