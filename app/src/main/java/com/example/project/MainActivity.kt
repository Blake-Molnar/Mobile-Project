package com.example.project

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.w3c.dom.Text
import kotlin.collections.ArrayList
import kotlin.concurrent.thread



class MainActivity : AppCompatActivity() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val baseURL = "https://2e.aonprd.com/"
        val list: ArrayList<Creature> = ArrayList()
        val saveList: ArrayList<Creature> = ArrayList()
        val tableCreature = findViewById<TableLayout>(R.id.creature_table)
        val selectedTable = findViewById<TableLayout>(R.id.selected_table)
        val xpTxt : TextView =  findViewById(R.id.xp)
        var i = 0
        var partyLevel = findViewById<EditText>(R.id.partyLevel)
        partyLevel.setText(1.toString())
        var partySize = findViewById<EditText>(R.id.partySize)
        partySize.setText(4.toString())
        var severity = findViewById<TextView>(R.id.severity)
        severity.setText("Trivial")
        var saveBtn = findViewById<Button>(R.id.save)
        var loadBtn = findViewById<Button>(R.id.load)
        var spinner = findViewById<Spinner>(R.id.spinner)


        thread {
            Log.i("start", "start")
            val doc =
                Jsoup.connect("https://2e.aonprd.com/Creatures.aspx").header("accept-encoding", "")
                    .maxBodySize(0).get().let {

                    for (row in it.select("table").last()!!.select("tr")) {
                        Log.i("start", "table loop")

                        val tmp = Creature()
//                        if (i == 10) {
//                            break
//                            // temporary so I don't have 900 rows
//                        }
                        val tds: Elements = row.select("td")
                        if (tds.size > 5 && tds.get(0).text() != "") {

                            tmp.Id = tds.get(0).select("a").attr("href")
                            tmp.name = (tds.get(0).text())
                            tmp.size = (tds.get(4).text())
                            tmp.level = (tds.get(7).text())
                            //.... and so on for the rest of attributes
                            list.add(tmp)
                            i++
                            Log.i("start", "row loop")
                        }

                    }

                    Log.i("start", "finish")
                }
        }.join()

            for (Creature in list) {
                /// programmagically add table rows to table
                val tr = TableRow(this)
                val btnId = ImageButton(this)
//                btnId.setText(Creature.cId)
                btnId.setOnClickListener{
                    val link = baseURL + Creature.Id
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(link)
                    startActivity(intent)
                }
//                btnId.width = 20
                val icon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_search, null)
                btnId.setImageDrawable(icon)
                val txtName = TextView(this)
                txtName.setText(Creature.name)
                val txtSize = TextView(this)
                txtSize.setText(Creature.size)
                val txtLevel = TextView(this)
                txtLevel.setText(Creature.level)
                tr.addView(btnId)
                tr.addView(txtName)
                tr.addView(txtSize)
                tr.addView(txtLevel)
                tableCreature.addView(tr)

                tr.setOnClickListener{
                    val newTr = TableRow(this)
                    val txtSelectedName = TextView(this)
                    var xp = xpCalc(txtLevel.text.toString().toInt())

                    xpTxt.text = (xpTxt.text.toString().toInt() + xp).toString()
                    txtSelectedName.setText(Creature.name)
                    val number = TextView(this)
                    number.setRawInputType(InputType.TYPE_CLASS_NUMBER)
                    number.text = "1"

                    saveList.add(Creature)

                    partyLevel.addTextChangedListener(object : TextWatcher {

                        override fun afterTextChanged(s: Editable) {}

                        override fun beforeTextChanged(s: CharSequence, start: Int,
                                                       count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence, start: Int,
                                                   before: Int, count: Int) {
                            if(s.isNotEmpty() && s[0] != '0') {
                                xpTxt.text = (xpTxt.text.toString()
                                    .toInt() - (xp * number.text.toString()
                                    .toInt())).toString()
                                xp = xpCalc(txtLevel.text.toString().toInt())
                                xpTxt.text = (xpTxt.text.toString()
                                    .toInt() + (xp * number.text.toString()
                                    .toInt())).toString()
                            }
                        }
                    })
                    val plusBtn = ImageButton(this)
                    val icon3: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_add_24, null)
                    plusBtn.setImageDrawable(icon3)
                    plusBtn.setOnClickListener{
                        number.setText((number.text.toString().toInt() + 1).toString())
                        xpTxt.text = (xpTxt.text.toString().toInt() + xp).toString()
                    }
                    val minusBtn = ImageButton(this)
                    val icon4: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_remove_24, null)
                    minusBtn.setImageDrawable(icon4)
                    minusBtn.setOnClickListener{
                        if(number.text.toString().toInt() > 1) {
                            number.setText((number.text.toString().toInt() - 1).toString())
                            xpTxt.text = (xpTxt.text.toString().toInt() - xp).toString()
                        }
                    }
                    val deleteBtn = ImageButton(this)
                    val icon2: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_delete_24, null)
                    deleteBtn.setImageDrawable(icon2)
                    deleteBtn.setOnClickListener{
                        if(!number.text.isEmpty())
                        {
                            xpTxt.text = ((xpTxt.text.toString().toInt() - (xp * number.text.toString().toInt() ))).toString()
                        }
                        newTr.removeAllViews()
                        selectedTable.removeView(newTr)
                        partyLevel.removeTextChangedListener(null)
                        saveList.remove(Creature)

                    }
                    newTr.addView(minusBtn)
                    newTr.addView(number)
                    newTr.addView(plusBtn)
                    newTr.addView(txtSelectedName)
                    newTr.addView(deleteBtn)
                    selectedTable.addView(newTr)

                }

                Log.i("start", "creature loop")


        }


        xpTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                    severity.text = (severityCalc(s.toString().toInt()))
                }
        })

        partySize.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if(s.isNotEmpty() && s[0] != '0') {
                    severity.text = (severityCalc(xpTxt.text.toString().toInt()))
                }
            }
        })

        saveBtn.setOnClickListener{
            var count = 0

            var name = findViewById<EditText>(R.id.encounterName).text.toString()
            if(name.isNotEmpty() && name != "Name here:" && saveList.isNotEmpty()) {
                db.collection("encounters").get()
                    .addOnSuccessListener { document ->
                        Log.i("start", "success")

                        count = document.size()
                        Log.i("start", count.toString())
                        Log.i("start", count.toString() + 1)

                        var saveMap = hashMapOf(
                            "creatures" to saveList
                        )
                        Log.i("outside", count.toString())

                        db.collection("encounters").document(name).set(saveMap)

                    }
                    .addOnFailureListener { exception ->
                        /// todo: exception display handle whatever
                        Log.i("fail", exception.message.toString())

                    }
            }else
            {
                if(saveList.isEmpty())
                {
                    Toast.makeText(this@MainActivity, "Please add creatures for this encounter", Toast.LENGTH_SHORT).show()

                }
                else{
                    Toast.makeText(this@MainActivity, "Please enter a name for this encounter", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadBtn.setOnClickListener{
            var selected = spinner.selectedItem.toString()

            db.collection("encounters").document(selected).get()
                .addOnSuccessListener { docs ->

                    var i = 0
                    var tmp = Creature()
                    for (x in docs.data!!.entries.elementAt(0).value.toString().split(","))
                    {
                        val regex = """(\{|\[|\}|\])""".toRegex()
                        var data = regex.replace(x, "").split("=")[1]
                        when ( i)
                        {
                            0 -> tmp.size = data
                            1 -> tmp.level = data
                            2 -> tmp.name = data
                            3 -> tmp.Id = data
                        }
                        if(i == 3)
                        {
                            i = 0

                            val newTr = TableRow(this)
                            val txtSelectedName = TextView(this)
                            var xp = xpCalc(tmp.level.toInt())

                            xpTxt.text = (xpTxt.text.toString().toInt() + xp).toString()
                            txtSelectedName.setText(tmp.name)
                            val number = TextView(this)
                            number.setRawInputType(InputType.TYPE_CLASS_NUMBER)
                            number.text = "1"

                            saveList.add(tmp)

                            partyLevel.addTextChangedListener(object : TextWatcher {

                                override fun afterTextChanged(s: Editable) {}

                                override fun beforeTextChanged(s: CharSequence, start: Int,
                                                               count: Int, after: Int) {
                                }

                                override fun onTextChanged(s: CharSequence, start: Int,
                                                           before: Int, count: Int) {
                                    if(s.isNotEmpty() && s[0] != '0') {
                                        xpTxt.text = (xpTxt.text.toString()
                                            .toInt() - (xp * number.text.toString()
                                            .toInt())).toString()
                                        xp = xpCalc(tmp.level.toInt())
                                        xpTxt.text = (xpTxt.text.toString()
                                            .toInt() + (xp * number.text.toString()
                                            .toInt())).toString()
                                    }
                                }
                            })
                            val plusBtn = ImageButton(this)
                            val icon3: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_add_24, null)
                            plusBtn.setImageDrawable(icon3)
                            plusBtn.setOnClickListener{
                                number.setText((number.text.toString().toInt() + 1).toString())
                                xpTxt.text = (xpTxt.text.toString().toInt() + xp).toString()
                            }
                            val minusBtn = ImageButton(this)
                            val icon4: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_remove_24, null)
                            minusBtn.setImageDrawable(icon4)
                            minusBtn.setOnClickListener{
                                if(number.text.toString().toInt() > 1) {
                                    number.setText((number.text.toString().toInt() - 1).toString())
                                    xpTxt.text = (xpTxt.text.toString().toInt() - xp).toString()
                                }
                            }
                            val deleteBtn = ImageButton(this)
                            val icon2: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_delete_24, null)
                            deleteBtn.setImageDrawable(icon2)
                            deleteBtn.setOnClickListener{
                                if(!number.text.isEmpty())
                                {
                                    xpTxt.text = ((xpTxt.text.toString().toInt() - (xp * number.text.toString().toInt() ))).toString()
                                }
                                newTr.removeAllViews()
                                selectedTable.removeView(newTr)
                                partyLevel.removeTextChangedListener(null)
                                saveList.remove(tmp)

                            }
                            newTr.addView(minusBtn)
                            newTr.addView(number)
                            newTr.addView(plusBtn)
                            newTr.addView(txtSelectedName)
                            newTr.addView(deleteBtn)
                            selectedTable.addView(newTr)

                        }else{i++}

                    }
                }
                .addOnFailureListener { exception ->
                    /// todo: exception display handle whatever
                    Log.i("fail", exception.message.toString())
                }
        }

        db.collection("encounters")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.i("outside", "!")

                    return@addSnapshotListener
                }
                Log.i("outside", value.toString())

                var nameList : ArrayList<String> = ArrayList()
                for (doc in value!!) {
                    Log.i("outside", doc.id)
                    nameList.add(doc.id)
                }
                Log.i("outside", "2")
                var adapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, nameList)
                spinner.adapter = adapter
            }

    }

    fun severityCalc(xp: Int): String
    {
        var partySize = findViewById<EditText>(R.id.partySize)
        val difference = partySize.text.toString().toInt() - 4

//        var trivial = 40 + (difference * 10)
        var low = 60 + (difference * 15)
        var moderate = 80 + (difference * 20)
        var severe = 120 + (difference * 30)
        var extreme = 160 + (difference * 40)

        var severity = "Trivial"

        when
        {
//            xp <= trivial -> severity = "Trivial"
            xp >= extreme -> severity = "Extreme"
            xp >= severe -> severity = "Severe"
            xp >= moderate -> severity = "Moderate"
            xp >= low  -> severity = "Low"
        }

        return severity
    }
    /// calculates xp of a mob
    fun xpCalc(level: Int): Int
    {
        var partyLevel = findViewById<EditText>(R.id.partyLevel)

        val difference = level - partyLevel.text.toString().toInt()
        var xp = 0
        when (difference)
        {
            -3 -> xp = 15
            -2 -> xp = 20
            -1 -> xp = 30
            0 -> xp = 40
            1 -> xp = 60
            2 -> xp = 80
            3 -> xp = 120
        }

        if(difference <= -4)
        {
            xp = 10
        }
        else if (difference >= 4)
        {
            xp = 160
        }

        return xp
    }
}

class Creature{
    var Id: String="-1"
    var name: String=""
    var size: String=""
    var level: String=""
}



