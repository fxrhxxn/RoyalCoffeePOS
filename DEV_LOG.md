Sat, Jun 13 at 5:56 PM
Mobile App Development Assignment.pdf
PDF
Assignment: creating a new mobile Application For Android.

I have an assignment, where I have to develop a Fully functioning Mobile app that solves a real business problem, or can be used by a business or for personal use.
This Assignment is aimed at assessing our knowledge about developing a mobile application as Software Developers. by taking all concerning factors into consideration such as UI/UX, Permission handling, Security, and so on.

The document uploaded outlines the details.

Act as a senior Software Engineer/ Mobile Application Developer to guide me through this project from A to Z.

before we get into it completely, Suggest some other ideas for this project. Ask me some questions to get a clearer understanding of where this is going.


Sun, Jun 28 at 12:48 PM
Pasted text.txt
Document
That's great,

however there are some modifications I would like to make at this point before going further.
1. The UI will be on Landscape orientation only for this version. This is better for POS workflow.
2. In the section 12.2 constraints, the application shall be used on only one device. This is to not complicate things for this version. Meaning that the app will be used on a single device.

The application shall already contain the credentials for the Admin when it will be first launch. Then the admin can proceed to create the accounts for the Cashiers/Baristas.

I have updated the SRS report and pasted the new version which I think will be better for this version.


I already included an ERD in the Database Design section. I uploaded it so you can have a look.

I just need an explanation for the ERD
Mon, Jul 13 at 2:12 PM
The Wireframes are already designed using Figma.
Mon, Jul 13 at 7:51 PM
Great!.

Before we move on to the next part.
There are some adjustments that I want to make at this point before moving on.

1. When a User will log in, they will immediately see the Sales / POS Screen and there is no need to get to the Home Screen for either the Cashier/Barista nor the Admin. This will reduce the need to additional pages. The Sales/POS Screen can be considered as the Home Page. I have uploaded the Wireframe of the Sales/POS display. On the left side there are icons which the user can use to get to the Sales Screen, Dashboard Screen, Inventory Screen, Settings Screen. For an Admin, they will have access to all the screens. For a Cashier/Barista, they will have access to only the Sales Screen and the Settings Screen. The rest of the icons will be hidden for them.

2. When a user chooses Mobile Payment/Juice, the app will not generate a QR code. Instead it will just wait for the user to confirm the payment. This is just to not complicate things more.

3. We will remove the option for the use of Camera for this app. In most cafes, there are no use of a Barcode Scanner for the products, so there is no need to scan the bar codes. Instead, all products will be in available for the user to choose from the Sales Screen immediately. The camera was intended to be used as a subsidiary for the Barcode Scanner as there won't be one available for this project. So, for this version, we shall remove the use of the camera completely. This will help reduce the need for accessing the camera and simplify this version of the app.

I will adjust the earlier phases in the report to adapt to the changes mentioned. Moving on to the next phases of this project we shall adapt these changes.

let me know what you understood so far about these changes. Then we move on

Mobile App Development Assignment Report.docx
Document
Alright, Let's stop with the documentation for now and we move on to the actual development. I have uploaded my report to this chat. Note that the report is not in the complete order that you have given nor contains all the report content.
I have kept what I think is most relevant for now.
You will also see that some things are missing like the Navigation Flow Design. I am still waiting on Christa to complete it then I will add it to the report.


Now. For the development part.
Act as an expert Senior Android Developer and Solutions Architect specializing in modern native Android development. I want to build the mobile application using Kotlin and Jetpack Compose and the other tools we talked about

To prevent context loss and broken code, we will build this app sequentially using small, modular steps. Do not write all the code at once. For each step, provide clear file paths, explain the logic briefly, and check if I am ready to proceed before moving on.

Think of me as someone who does not know anything about the development of an Android App as this is literally the first time I have to do this. I have no prior experience to hands on practice in developing an app or any development project whatsoever. As an expert in the matter, you will guide me through it and help me understand what each components mean and what they do. You will also help me understand the basics of Kotlin, Jetpack compose so that I can properly master it.

At the end, I intent to properly develop a developer's mindset and skills in building android applications.


Worked for 27s

Absolutely. We will now switch from documentation mode to development mode.

I reviewed your current report and it already gives us a good development foundation: Android Studio, Kotlin, Room/SQLite, Firebase Firestore/Storage, Jetpack-style architecture, Figma wireframes, and a module-by-module development plan are already documented in your implementation section.

One important implementation decision before we start: even though the report mentions Firebase Authentication in one place, for this app we will use local username + PIN authentication with Room/SQLite, because that matches your final scope. Firebase will be used later mainly for receipt backup/synchronization, not for login.

We will build the app slowly and safely.

How we will work from now on

For each development step, I will give you:

Goal of the step
What you are learning
Exact files/folders involved
Code only for that step
What to run/test
A checkpoint before moving on

We will not build everything at once.

Our development order

We will follow this order:

Step 1: Create Android Studio project
Step 2: Lock landscape orientation
Step 3: Understand the basic project structure
Step 4: Create app theme and base UI
Step 5: Create navigation structure
Step 6: Build Login screen UI
Step 7: Create fake login logic temporarily
Step 8: Build Sales/POS screen UI
Step 9: Add product/category sample data
Step 10: Add cart/ticket logic
Step 11: Add payment screens
Step 12: Add Room database
Step 13: Replace fake data with database data
Step 14: Add receipt generation
Step 15: Add inventory screen
Step 16: Add dashboard screen
Step 17: Add settings screen
Step 18: Add notifications
Step 19: Add Firebase sync
Step 20: Test, polish, APK build

We will start simple. At first, the app will use temporary sample data. Later, we connect Room database. This prevents the project from becoming too complex too early.

What we are using

We will build with:

Language: Kotlin
UI: Jetpack Compose
Architecture: MVVM-inspired
Local DB: Room / SQLite
Cloud later: Firebase Firestore
IDE: Android Studio
Target device: Android tablet
Orientation: Landscape only

Jetpack Compose is Android’s modern toolkit for building native UI, and Android’s current project templates support creating Compose-enabled projects directly from Android Studio. Kotlin is also the recommended language for new Android projects and is required for modern frameworks like Jetpack Compose.

Step 1 — Create the Android Studio Project
Goal

Create the empty Android project that will become Royal Coffee POS.

What you will learn

You will learn:

What an Android project is
What a package name is
Why we use Kotlin
Why we choose Empty Activity
How the first app screen is created
Instructions

Open Android Studio, then:

File
→ New
→ New Project

Choose:

Template: Empty Activity

Use these settings:

Name: Royal Coffee POS
Package name: com.royalcoffee.pos
Save location: Choose your preferred folder
Language: Kotlin
Minimum SDK: API 26 or above
Build configuration language: Kotlin DSL

Then click Finish.

Android Studio will generate the starting project for us.

Why Empty Activity?

We choose Empty Activity because we want a clean Jetpack Compose project without unnecessary starter screens. We will build every screen ourselves: Login, Sales/POS, Payment, Receipt, Dashboard, Inventory, and Settings.

What you should see after project creation

After Android Studio finishes loading, you should see files such as:

MainActivity.kt
build.gradle.kts
AndroidManifest.xml
ui/theme/

The most important file at the beginning is:

app/src/main/java/com/royalcoffee/pos/MainActivity.kt

This is the first Kotlin file that runs when the app starts.

Checkpoint

Please create the project first. Once it opens successfully in Android Studio, tell me:

Project created successfully

Then we will move to Step 2: Lock the app to landscape orientation and understand MainActivity.kt.

android:screenOrientation="landscape"

I added the line, but there is as strikethrough throughout the line. Then it suggested another line " android:configChanges="orientation|screenSize|keyboardHidden""

There is the fill code in the AndroidManifest.xml

"""
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.RoyalCoffeePOS">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="landscape"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:label="@string/app_name"
            android:theme="@style/Theme.RoyalCoffeePOS"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>"""
Alright,
I ran the app again and the virtual device opened as you can see in the screenshot


login fields are working.
There are some changes to the theme.

The Royal Coffee POS app needs to have a sense of royalty to the app. Like the colors such as Royal Green and some golden accent.

Is this something which can be done afterwards or we can do it now?
Okay Great
Let's move on
Okay Great.

Great
Item are added to the cart and the total is calculated
Pasted text.txt
Document
Here is the complete SalesScreen.kt
Check for any errors


Pasted text(1).txt
Document
Here is the entire LoginScreen.kt

the layout is still not dynamic. I have uploaded a screenshot from the samsung tablet.
There are some things I spot.

1. The texts of the screen and the status bar overlaps. There should be some space between the texts and the status bar (Top left corner of the screen you can see the details of the status bar overlaps. This should be corrected.

2. The logout button at the bottom is not correct.

The entire screen with the entire components of the screen should be dynamic and adapt to different screen sizes.

Additionally, the screen should consider the navigation bar at the bottom as some devices have a navigation bar which is there constantly. so this should be kept in mind when working with screen on the app. Some navigation bars can be hidden providing more screen for the app. Then the app should dynamically take all the space. when the navigation bar is locked on the screen. the app should take the space of the screen minus the navigation bar size.
Pasted text(2).txt
Document
Find the mistake


Pasted text(6).txt
Document
Check the ReceiptPreviewDialog.kt
"""
package com.royalcoffee.pos.ui.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.ui.theme.RoyalGold

@Composable
fun ReceiptPreviewDialog(
receiptNumber: String,
receiptDateTime: String,
cartItems: List<CartItem>,
subtotal: Int,
discount: Int,
tax: Int,
total: Int,
paymentMethod: PaymentMethod,
amountPaid: Int?,
changeGiven: Int?,
onClose: () -> Unit,
onNextOrder: () -> Unit
) {
AlertDialog(
onDismissRequest = onClose,
title = {
Text(
text = "Receipt Preview",
style = MaterialTheme.typography.titleLarge,
fontWeight = FontWeight.SemiBold,
textAlign = TextAlign.Center,
modifier = Modifier.fillMaxWidth()
)
},
text = {
ReceiptContent(
receiptNumber = receiptNumber,
receiptDateTime = receiptDateTime,
cartItems = cartItems,
subtotal = subtotal,
discount = discount,
tax = tax,
total = total,
paymentMethod = paymentMethod,
amountPaid = amountPaid,
changeGiven = changeGiven
)
},
confirmButton = {
Button(
onClick = onNextOrder
) {
Text(text = "Next Order")
}
},
dismissButton = {
OutlinedButton(
onClick = onClose
) {
Text(text = "Close")
}
}
)
}

@Composable
private fun ReceiptContent(
receiptNumber: String,
receiptDateTime: String,
cartItems: List<CartItem>,
subtotal: Int,
discount: Int,
tax: Int,
total: Int,
paymentMethod: PaymentMethod,
amountPaid: Int?,
changeGiven: Int?
) {
Card(
modifier = Modifier
.fillMaxWidth()
.heightIn(max = 520.dp),
shape = RoundedCornerShape(18.dp),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.background
),
border = BorderStroke(
width = 1.dp,
color = RoyalGold.copy(alpha = 0.45f)
)
) {
Column(
modifier = Modifier
.fillMaxWidth()
.verticalScroll(rememberScrollState())
.padding(20.dp)
) {
ReceiptHeader(
receiptNumber = receiptNumber,
receiptDateTime = receiptDateTime
)

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Items",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            cartItems.forEach { item ->
                ReceiptItemRow(cartItem = item)
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReceiptSummaryRow(
                label = "Subtotal",
                value = "Rs $subtotal"
            )

            ReceiptSummaryRow(
                label = "Discount",
                value = "Rs $discount"
            )

            ReceiptSummaryRow(
                label = "Tax",
                value = "Rs $tax"
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ReceiptTotalRow(total = total)

            Spacer(modifier = Modifier.height(18.dp))

            PaymentSection(
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                changeGiven = changeGiven
            )

            Spacer(modifier = Modifier.height(18.dp))

            ReceiptFooter()
        }
    }
}

@Composable
private fun ReceiptHeader(
receiptNumber: String,
receiptDateTime: String
) {
Column(
modifier = Modifier.fillMaxWidth(),
horizontalAlignment = Alignment.CenterHorizontally
) {
Surface(
shape = RoundedCornerShape(50),
color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
border = BorderStroke(
width = 1.dp,
color = RoyalGold.copy(alpha = 0.7f)
)
) {
Text(
text = "RC",
modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.Bold,
color = MaterialTheme.colorScheme.primary
)
}

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Royal Coffee POS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Receipt No: $receiptNumber",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Date/Time: $receiptDateTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReceiptItemRow(
cartItem: CartItem
) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(vertical = 6.dp)
) {
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween
) {
Column(
modifier = Modifier.weight(1f)
) {
Text(
text = cartItem.product.name,
style = MaterialTheme.typography.bodyMedium,
fontWeight = FontWeight.SemiBold
)

                Text(
                    text = "Qty ${cartItem.quantity} x Rs ${cartItem.product.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Text(
                text = "Rs ${cartItem.lineTotal}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ReceiptSummaryRow(
label: String,
value: String
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(vertical = 3.dp),
horizontalArrangement = Arrangement.SpaceBetween
) {
Text(
text = label,
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
)

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReceiptTotalRow(
total: Int
) {
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically
) {
Text(
text = "Total",
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.Bold
)

        Text(
            text = "Rs $total",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PaymentSection(
paymentMethod: PaymentMethod,
amountPaid: Int?,
changeGiven: Int?
) {
Column(
modifier = Modifier
.fillMaxWidth()
.background(
color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
shape = RoundedCornerShape(14.dp)
)
.padding(14.dp)
) {
Text(
text = "Payment",
style = MaterialTheme.typography.titleSmall,
fontWeight = FontWeight.SemiBold,
color = MaterialTheme.colorScheme.primary
)

        Spacer(modifier = Modifier.height(8.dp))

        ReceiptSummaryRow(
            label = "Method",
            value = paymentMethod.name
        )

        if (amountPaid != null) {
            ReceiptSummaryRow(
                label = "Amount Paid",
                value = "Rs $amountPaid"
            )
        }

        if (changeGiven != null && changeGiven > 0) {
            ReceiptSummaryRow(
                label = "Change",
                value = "Rs $changeGiven"
            )
        }
    }
}

@Composable
private fun ReceiptFooter() {
Box(
modifier = Modifier.fillMaxWidth(),
contentAlignment = Alignment.Center
) {
Text(
text = "Thank you for your purchase.",
style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
textAlign = TextAlign.Center
)
}
}

"""


The SalesScreen.kt uploaded in the pasted text
The action buttons are to be saved at the bottom next to the Next order button and the close button.
So I need a print receipt button which is the main button, then share on whatsapp and then download receipt button then the next order button and close button
In the receipt preview screen, I want to see a preview of how the printed receipt will look like.
For example, look at the uploaded picture which i took online. I want the receipt in the receipt preview to look like an actual receipt (and not taking up the whole page). What can be a solution for that?
Suggest some ideas then I will choose one then we move on
Pasted text(7).txt
Document
That is great.
I have made some adjustments I removed the barcode. This is not useful for this version.
There are some changes to be made. I pasted the current ReceiptPreviewDialog.kt in the chat.

in the preview screen, I want the buttons to be on the right side of the screen in a column.
The close button should be at the top to prevent the user from accidentally closing the preview.
The other buttons are at the bottom large and distinct.
---------
Close


Download
Share via WhatsApp
Print Receipt
Next Order
---------
Is this the message supposed to be shown?
Before we move on to the next step Step 13 — Add Main Screen Navigation., I want to make some little adjustments.

First, after confirming payment, instead of tapping on the "Generate Receipt" button, it moves directly to the Receipt Preview screen. This skips one unnecessary step and directly goes in to the Receipt Preview screen, enabling faster workflow.

Secondly, as you can see in the image uploaded, the Receipt title shows "ROYAL COFFEE POS". This is wrong as it should display only the company name which is "Royal Coffee" and I want that font to be the same as the font we used in the Login Page which is the Cinzel font
Thursday 7:44 PM
DEV_LOG.md
File
This is the current DEV_LOG.md which was done previously.
Write a new updated one based on everything that has been done so far and include the updates made.
Pasted text(9).txt
Document
Here is the entire SalesScreen.kt in the pasted text


Monday 8:31 PM
Pasted text(10).txt
Document
I did the adjustments you mentioned and also added some of my own
Pasted text(12).txt
Document
This is the entire sales screen
kindly find the error and update it.

If there are additional changes that you did not mention and it is present, keep them.


Pasted text(13).txt
Document

Great. Next we add an Admin-only Receipt History screen so saved receipts from Room can be viewed inside the app.

Your current SalesScreen.kt already uses the main AppScreen navigation structure, so we will add one more screen called Receipts to that navigation.

Before starting, commit Step 22 first:

git status
git add .
git commit -m "Save completed receipts to Room"
git push
Step 23 — Add Receipt History Screen

This version will show saved receipt headers:

Receipt number
Date/time
Cashier
Payment method
Total

Receipt line-item details can come in the next step.

Step 23.1 — Create ReceiptHistoryItem.kt

Create this file:

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptHistoryItem.kt

Add:

package com.royalcoffee.pos.ui.receipt

/**
* ReceiptHistoryItem is the UI model used by the Receipt History screen.
*
* It is separate from ReceiptHeaderEntity because the UI should not depend
* directly on database table classes.
  */
  data class ReceiptHistoryItem(
  val receiptId: Int,
  val receiptNumber: String,
  val receiptDateTime: String,
  val cashierName: String,
  val total: Int,
  val paymentMethod: String
  )
  Step 23.2 — Update ReceiptRepository.kt

Open:

app/src/main/java/com/royalcoffee/pos/data/local/repository/ReceiptRepository.kt

Add this import:

import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import kotlinx.coroutines.flow.Flow

Inside ReceiptRepository, add this property near the top:

/**
* Returns all saved receipt headers.
*
* This will be used by the Receipt History screen.
  */
  val allReceiptHeaders: Flow<List<ReceiptHeaderEntity>> =
  receiptDao.getAllReceiptHeaders()
  Step 23.3 — Create ReceiptHistoryViewModel.kt

Create this file:

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptHistoryViewModel.kt

Add:

package com.royalcoffee.pos.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
* ReceiptHistoryViewModel exposes saved receipts from Room to the UI.
*
* Flow:
* UI -> ViewModel -> Repository -> DAO -> Room database
  */
  class ReceiptHistoryViewModel(
  private val receiptRepository: ReceiptRepository
  ) : ViewModel() {

  /**
    * Saved receipt headers shown in the Receipt History screen.
      */
      val receiptHistory: StateFlow<List<ReceiptHistoryItem>> =
      receiptRepository.allReceiptHeaders
      .map { receiptHeaders ->
      receiptHeaders.map { receiptHeader ->
      receiptHeader.toReceiptHistoryItem()
      }
      }
      .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptyList()
      )
      }

/**
* Converts a database ReceiptHeaderEntity into a UI-friendly ReceiptHistoryItem.
  */
  private fun ReceiptHeaderEntity.toReceiptHistoryItem(): ReceiptHistoryItem {
  return ReceiptHistoryItem(
  receiptId = receiptId,
  receiptNumber = receiptNumber,
  receiptDateTime = receiptDateTime,
  cashierName = cashierName,
  total = total,
  paymentMethod = paymentMethod
  )
  }

/**
* Factory used to create ReceiptHistoryViewModel manually.
*
* We need this because the ViewModel requires a ReceiptRepository parameter.
  */
  class ReceiptHistoryViewModelFactory(
  private val receiptRepository: ReceiptRepository
  ) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(
  modelClass: Class<T>
  ): T {
  if (modelClass.isAssignableFrom(ReceiptHistoryViewModel::class.java)) {
  return ReceiptHistoryViewModel(
  receiptRepository = receiptRepository
  ) as T
  }

       throw IllegalArgumentException("Unknown ViewModel class")
  }
  }
  Step 23.4 — Create ReceiptHistoryScreen.kt

Create this file:

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptHistoryScreen.kt

Add:

package com.royalcoffee.pos.ui.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold

/**
* ReceiptHistoryScreen displays saved receipts from the Room database.
*
* Version 1:
* - Shows receipt headers only.
*
* Next version:
* - Tap a receipt to view full receipt details.
    */
    @Composable
    fun ReceiptHistoryScreen(
    receipts: List<ReceiptHistoryItem>,
    modifier: Modifier = Modifier
    ) {
    Surface(
    modifier = modifier.fillMaxHeight(),
    color = MaterialTheme.colorScheme.background
    ) {
    Column(
    modifier = Modifier
    .fillMaxHeight()
    .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally
    ) {
    Text(
    text = "Receipt History",
    fontFamily = CinzelFontFamily,
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.headlineMedium,
    color = MaterialTheme.colorScheme.primary,
    textAlign = TextAlign.Center
    )

           Text(
               text = "View completed transactions saved locally on this device.",
               style = MaterialTheme.typography.bodyLarge,
               color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
               textAlign = TextAlign.Center,
               modifier = Modifier.padding(top = 8.dp)
           )

           Spacer(modifier = Modifier.height(28.dp))

           Column(
               modifier = Modifier.widthIn(max = 960.dp)
           ) {
               if (receipts.isEmpty()) {
                   EmptyReceiptHistoryCard()
               } else {
                   ReceiptHistoryListCard(
                       receipts = receipts
                   )
               }
           }
    }
    }
    }

@Composable
private fun EmptyReceiptHistoryCard() {
Card(
modifier = Modifier.fillMaxWidth(),
shape = RoundedCornerShape(22.dp),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
),
border = BorderStroke(
width = 1.dp,
color = RoyalGold.copy(alpha = 0.35f)
)
) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(28.dp),
horizontalAlignment = Alignment.CenterHorizontally
) {
Text(
text = "No saved receipts yet",
style = MaterialTheme.typography.titleLarge,
fontWeight = FontWeight.SemiBold,
color = MaterialTheme.colorScheme.primary,
textAlign = TextAlign.Center
)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Complete an order and tap Next Order to save a receipt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReceiptHistoryListCard(
receipts: List<ReceiptHistoryItem>
) {
Card(
modifier = Modifier.fillMaxWidth(),
shape = RoundedCornerShape(22.dp),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.surface
),
border = BorderStroke(
width = 1.dp,
color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
)
) {
Column(
modifier = Modifier.padding(22.dp)
) {
Text(
text = "Saved Receipts",
style = MaterialTheme.typography.titleLarge,
color = MaterialTheme.colorScheme.primary,
fontWeight = FontWeight.SemiBold
)

            Text(
                text = "${receipts.size} receipt(s) saved locally.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            LazyColumn {
                items(receipts) { receipt ->
                    ReceiptHistoryRow(
                        receipt = receipt
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptHistoryRow(
receipt: ReceiptHistoryItem
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(vertical = 14.dp),
verticalAlignment = Alignment.CenterVertically
) {
Column(
modifier = Modifier.weight(1.5f)
) {
Text(
text = receipt.receiptNumber,
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.SemiBold,
color = MaterialTheme.colorScheme.primary
)

            Text(
                text = receipt.receiptDateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }

        Text(
            text = receipt.cashierName,
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = receipt.paymentMethod,
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Rs ${receipt.total}",
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}
Step 23.5 — Update AppScreen.kt

Open:

app/src/main/java/com/royalcoffee/pos/model/AppScreen.kt

Add RECEIPTS.

Your enum should become:

package com.royalcoffee.pos.model

/**
* AppScreen represents the main screens available after login.
*
* This enum helps the app know which screen should be displayed
* when the user taps an item in the side navigation menu.
  */
  enum class AppScreen(
  val title: String
  ) {
  SALES("Sales"),
  DASHBOARD("Dashboard"),
  INVENTORY("Inventory"),
  RECEIPTS("Receipts"),
  SETTINGS("Settings")
  }
  Step 23.6 — Update SalesScreen.kt

Add these imports:

import com.royalcoffee.pos.ui.receipt.ReceiptHistoryScreen
import com.royalcoffee.pos.ui.receipt.ReceiptHistoryViewModel
import com.royalcoffee.pos.ui.receipt.ReceiptHistoryViewModelFactory

After you create receiptRepository, add this:

/**
* Create the ReceiptHistoryViewModel.
*
* This ViewModel exposes saved receipt headers to the Receipt History screen.
  */
  val receiptHistoryViewModel: ReceiptHistoryViewModel = viewModel(
  factory = ReceiptHistoryViewModelFactory(
  receiptRepository = receiptRepository
  )
  )

/**
* Saved receipts displayed in the Receipt History screen.
  */
  val receiptHistoryItems by receiptHistoryViewModel.receiptHistory.collectAsStateWithLifecycle()
  Step 23.7 — Add Receipts to the side navigation

Inside SideNavigation(), after Inventory, add:

NavigationItem(
text = "Receipts",
isSelected = currentScreen == AppScreen.RECEIPTS,
onClick = {
onScreenSelected(AppScreen.RECEIPTS)
}
)

So your Admin block should become:

if (userRole == UserRole.ADMIN) {
NavigationItem(
text = "Dashboard",
isSelected = currentScreen == AppScreen.DASHBOARD,
onClick = {
onScreenSelected(AppScreen.DASHBOARD)
}
)

    NavigationItem(
        text = "Inventory",
        isSelected = currentScreen == AppScreen.INVENTORY,
        onClick = {
            onScreenSelected(AppScreen.INVENTORY)
        }
    )

    NavigationItem(
        text = "Receipts",
        isSelected = currentScreen == AppScreen.RECEIPTS,
        onClick = {
            onScreenSelected(AppScreen.RECEIPTS)
        }
    )
}
Step 23.8 — Add Receipts to the screen switch

Inside your when (currentScreen) block in SalesScreen.kt, add:

AppScreen.RECEIPTS -> {
ReceiptHistoryScreen(
receipts = receiptHistoryItems,
modifier = Modifier
.weight(1f)
.fillMaxHeight()
)
}

Place it after AppScreen.INVENTORY.

Expected result

Admin side navigation:

Sales
Dashboard
Inventory
Receipts
Settings

Barista side navigation:

Sales
Settings

Test flow:

1. Login as Admin.
2. Open Receipts.
3. It should say no saved receipts yet.
4. Go to Sales.
5. Complete an order.
6. Tap Next Order.
7. Go back to Receipts.
8. The saved receipt should appear.

Then commit:

git status
git add .
git commit -m "Add receipt history screen"
git push
next

Great. Since we added Receipt History, the next part is:

Step 24 — Tap a Saved Receipt to View Its Details

Right now, the Receipt History screen shows only the receipt header:

Receipt number
Date/time
Cashier
Payment method
Total

Now we will make each saved receipt clickable. When Admin taps a receipt, the app will show the product lines inside that receipt.

Your current SalesScreen.kt already has the navigation structure and passes receipt history data into the Receipts screen, so this is a natural next upgrade.

Step 24.1 — Create ReceiptDetailItem.kt

Create this file:

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptDetailItem.kt

Add:

package com.royalcoffee.pos.ui.receipt

/**
* ReceiptDetailItem is the UI model for one product line
* inside a saved receipt.
*
* Example:
* 2 x Latte at Rs 120 = Rs 240
  */
  data class ReceiptDetailItem(
  val productName: String,
  val quantity: Int,
  val unitPrice: Int,
  val lineTotal: Int
  )
  Step 24.2 — Update ReceiptDao.kt

Open:

app/src/main/java/com/royalcoffee/pos/data/local/dao/ReceiptDao.kt

Add this function inside ReceiptDao:

/**
* Returns all detail rows for one receipt as a one-time list.
*
* This is useful when the Admin taps a receipt in the history screen.
  */
  @Query(
  """
  SELECT *
  FROM receipt_details
  WHERE receipt_id = :receiptId
  ORDER BY receipt_detail_id ASC
  """
  )
  suspend fun getReceiptDetailsOnce(
  receiptId: Int
  ): List<ReceiptDetailEntity>

Keep your existing Flow version. We are adding this one-time version because tapping a receipt is a simple action.

Step 24.3 — Update ReceiptRepository.kt

Open:

app/src/main/java/com/royalcoffee/pos/data/local/repository/ReceiptRepository.kt

Add this function inside ReceiptRepository:

/**
* Returns receipt detail rows for one saved receipt.
*
* Used when Admin taps a receipt from Receipt History.
  */
  suspend fun getReceiptDetailsOnce(
  receiptId: Int
  ): List<ReceiptDetailEntity> {
  return receiptDao.getReceiptDetailsOnce(
  receiptId = receiptId
  )
  }

Your file already imports ReceiptDetailEntity because saveReceipt() uses it, so you should not need a new import.

Step 24.4 — Replace ReceiptHistoryViewModel.kt

Open:

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptHistoryViewModel.kt

Replace the full file with:

package com.royalcoffee.pos.ui.receipt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
* ReceiptHistoryViewModel exposes saved receipts from Room to the UI.
*
* Flow:
* UI -> ViewModel -> Repository -> DAO -> Room database
  */
  class ReceiptHistoryViewModel(
  private val receiptRepository: ReceiptRepository
  ) : ViewModel() {

  /**
    * Saved receipt headers shown in the Receipt History screen.
      */
      val receiptHistory: StateFlow<List<ReceiptHistoryItem>> =
      receiptRepository.allReceiptHeaders
      .map { receiptHeaders ->
      receiptHeaders.map { receiptHeader ->
      receiptHeader.toReceiptHistoryItem()
      }
      }
      .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptyList()
      )

  /**
    * The receipt selected by the Admin.
      */
      var selectedReceipt by mutableStateOf<ReceiptHistoryItem?>(null)
      private set

  /**
    * Detail rows belonging to the selected receipt.
      */
      var selectedReceiptDetails by mutableStateOf<List<ReceiptDetailItem>>(emptyList())
      private set

  /**
    * Controls whether the receipt detail dialog is visible.
      */
      var isReceiptDetailDialogVisible by mutableStateOf(false)
      private set

  /**
    * Called when the Admin taps a saved receipt row.
    *
    * This loads the receipt detail rows from Room and opens the dialog.
      */
      fun openReceiptDetails(receipt: ReceiptHistoryItem) {
      selectedReceipt = receipt

      viewModelScope.launch {
      selectedReceiptDetails = receiptRepository
      .getReceiptDetailsOnce(receipt.receiptId)
      .map { receiptDetailEntity ->
      receiptDetailEntity.toReceiptDetailItem()
      }

           isReceiptDetailDialogVisible = true
      }
      }

  /**
    * Closes the receipt detail dialog.
      */
      fun closeReceiptDetails() {
      isReceiptDetailDialogVisible = false
      selectedReceipt = null
      selectedReceiptDetails = emptyList()
      }
      }

/**
* Converts a database ReceiptHeaderEntity into a UI-friendly ReceiptHistoryItem.
  */
  private fun ReceiptHeaderEntity.toReceiptHistoryItem(): ReceiptHistoryItem {
  return ReceiptHistoryItem(
  receiptId = receiptId,
  receiptNumber = receiptNumber,
  receiptDateTime = receiptDateTime,
  cashierName = cashierName,
  total = total,
  paymentMethod = paymentMethod
  )
  }

/**
* Converts a database ReceiptDetailEntity into a UI-friendly ReceiptDetailItem.
  */
  private fun ReceiptDetailEntity.toReceiptDetailItem(): ReceiptDetailItem {
  return ReceiptDetailItem(
  productName = productName,
  quantity = quantity,
  unitPrice = unitPrice,
  lineTotal = lineTotal
  )
  }

/**
* Factory used to create ReceiptHistoryViewModel manually.
*
* We need this because the ViewModel requires a ReceiptRepository parameter.
  */
  class ReceiptHistoryViewModelFactory(
  private val receiptRepository: ReceiptRepository
  ) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(
  modelClass: Class<T>
  ): T {
  if (modelClass.isAssignableFrom(ReceiptHistoryViewModel::class.java)) {
  return ReceiptHistoryViewModel(
  receiptRepository = receiptRepository
  ) as T
  }

       throw IllegalArgumentException("Unknown ViewModel class")
  }
  }
  Step 24.5 — Replace ReceiptHistoryScreen.kt

Open:

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptHistoryScreen.kt

Replace the full file with:

package com.royalcoffee.pos.ui.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold

/**
* ReceiptHistoryScreen displays saved receipts from the Room database.
*
* Version 2:
* - Shows receipt headers.
* - Allows Admin to tap a receipt and view product details.
    */
    @Composable
    fun ReceiptHistoryScreen(
    receipts: List<ReceiptHistoryItem>,
    selectedReceipt: ReceiptHistoryItem?,
    selectedReceiptDetails: List<ReceiptDetailItem>,
    isDetailDialogVisible: Boolean,
    onReceiptClick: (ReceiptHistoryItem) -> Unit,
    onCloseDetailDialog: () -> Unit,
    modifier: Modifier = Modifier
    ) {
    Surface(
    modifier = modifier.fillMaxHeight(),
    color = MaterialTheme.colorScheme.background
    ) {
    Column(
    modifier = Modifier
    .fillMaxHeight()
    .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally
    ) {
    Text(
    text = "Receipt History",
    fontFamily = CinzelFontFamily,
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.headlineMedium,
    color = MaterialTheme.colorScheme.primary,
    textAlign = TextAlign.Center
    )

           Text(
               text = "View completed transactions saved locally on this device.",
               style = MaterialTheme.typography.bodyLarge,
               color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
               textAlign = TextAlign.Center,
               modifier = Modifier.padding(top = 8.dp)
           )

           Spacer(modifier = Modifier.height(28.dp))

           Column(
               modifier = Modifier.widthIn(max = 960.dp)
           ) {
               if (receipts.isEmpty()) {
                   EmptyReceiptHistoryCard()
               } else {
                   ReceiptHistoryListCard(
                       receipts = receipts,
                       onReceiptClick = onReceiptClick
                   )
               }
           }
    }
    }

if (isDetailDialogVisible && selectedReceipt != null) {
ReceiptDetailDialog(
receipt = selectedReceipt,
details = selectedReceiptDetails,
onDismiss = onCloseDetailDialog
)
}
}

@Composable
private fun EmptyReceiptHistoryCard() {
Card(
modifier = Modifier.fillMaxWidth(),
shape = RoundedCornerShape(22.dp),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
),
border = BorderStroke(
width = 1.dp,
color = RoyalGold.copy(alpha = 0.35f)
)
) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(28.dp),
horizontalAlignment = Alignment.CenterHorizontally
) {
Text(
text = "No saved receipts yet",
style = MaterialTheme.typography.titleLarge,
fontWeight = FontWeight.SemiBold,
color = MaterialTheme.colorScheme.primary,
textAlign = TextAlign.Center
)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Complete an order and tap Next Order to save a receipt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReceiptHistoryListCard(
receipts: List<ReceiptHistoryItem>,
onReceiptClick: (ReceiptHistoryItem) -> Unit
) {
Card(
modifier = Modifier.fillMaxWidth(),
shape = RoundedCornerShape(22.dp),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.surface
),
border = BorderStroke(
width = 1.dp,
color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
)
) {
Column(
modifier = Modifier.padding(22.dp)
) {
Text(
text = "Saved Receipts",
style = MaterialTheme.typography.titleLarge,
color = MaterialTheme.colorScheme.primary,
fontWeight = FontWeight.SemiBold
)

            Text(
                text = "${receipts.size} receipt(s) saved locally. Tap a receipt to view details.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            LazyColumn {
                items(receipts) { receipt ->
                    ReceiptHistoryRow(
                        receipt = receipt,
                        onClick = {
                            onReceiptClick(receipt)
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptHistoryRow(
receipt: ReceiptHistoryItem,
onClick: () -> Unit
) {
Row(
modifier = Modifier
.fillMaxWidth()
.clickable {
onClick()
}
.padding(vertical = 14.dp),
verticalAlignment = Alignment.CenterVertically
) {
Column(
modifier = Modifier.weight(1.5f)
) {
Text(
text = receipt.receiptNumber,
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.SemiBold,
color = MaterialTheme.colorScheme.primary
)

            Text(
                text = receipt.receiptDateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }

        Text(
            text = receipt.cashierName,
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = receipt.paymentMethod,
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Rs ${receipt.total}",
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ReceiptDetailDialog(
receipt: ReceiptHistoryItem,
details: List<ReceiptDetailItem>,
onDismiss: () -> Unit
) {
AlertDialog(
onDismissRequest = onDismiss,
title = {
Text(
text = receipt.receiptNumber,
fontWeight = FontWeight.Bold,
color = MaterialTheme.colorScheme.primary
)
},
text = {
Column(
modifier = Modifier.widthIn(max = 680.dp)
) {
Text(
text = "${receipt.receiptDateTime} • ${receipt.paymentMethod} • Rs ${receipt.total}",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
)

                Spacer(modifier = Modifier.height(14.dp))

                DetailHeaderRow(
                    first = "Product",
                    second = "Qty",
                    third = "Line Total"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (details.isEmpty()) {
                    Text(
                        text = "No receipt line details found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                } else {
                    details.forEach { detail ->
                        DetailDataRow(
                            first = "${detail.productName}\nRs ${detail.unitPrice} each",
                            second = detail.quantity.toString(),
                            third = "Rs ${detail.lineTotal}"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "Close")
            }
        }
    )
}

@Composable
private fun DetailHeaderRow(
first: String,
second: String,
third: String
) {
Row(
modifier = Modifier.fillMaxWidth()
) {
Text(
text = first,
modifier = Modifier.weight(1.5f),
style = MaterialTheme.typography.bodySmall,
fontWeight = FontWeight.Bold,
color = MaterialTheme.colorScheme.primary
)

        Text(
            text = second,
            modifier = Modifier.weight(0.5f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = third,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailDataRow(
first: String,
second: String,
third: String
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(vertical = 7.dp),
verticalAlignment = Alignment.CenterVertically
) {
Text(
text = first,
modifier = Modifier.weight(1.5f),
style = MaterialTheme.typography.bodyMedium,
fontWeight = FontWeight.Medium
)

        Text(
            text = second,
            modifier = Modifier.weight(0.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = third,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}
Step 24.6 — Update the ReceiptHistoryScreen call in SalesScreen.kt

Find this block in your when (currentScreen):

AppScreen.RECEIPTS -> {
ReceiptHistoryScreen(
receipts = receiptHistoryItems,
modifier = Modifier
.weight(1f)
.fillMaxHeight()
)
}

Replace it with:

AppScreen.RECEIPTS -> {
ReceiptHistoryScreen(
receipts = receiptHistoryItems,
selectedReceipt = receiptHistoryViewModel.selectedReceipt,
selectedReceiptDetails = receiptHistoryViewModel.selectedReceiptDetails,
isDetailDialogVisible = receiptHistoryViewModel.isReceiptDetailDialogVisible,
onReceiptClick = { receipt ->
receiptHistoryViewModel.openReceiptDetails(receipt)
},
onCloseDetailDialog = {
receiptHistoryViewModel.closeReceiptDetails()
},
modifier = Modifier
.weight(1f)
.fillMaxHeight()
)
}
Test checklist
1. Login as Admin.
2. Create an order.
3. Confirm payment.
4. Tap Next Order.
5. Go to Receipts.
6. Tap the saved receipt.
7. Confirm a dialog opens.
8. Confirm the products sold in that receipt are displayed.
9. Close the dialog.
10. Open another receipt if available.

Then commit:

git status
git add .
git commit -m "Add receipt detail view"
git push

Next after this, we should update the Dashboard so sales totals and payment breakdown are calculated from saved receipts instead of sample data.

Pasted text(14).txt
Document
Alright. Let's take a pause here.

can you generate a full DEV_LOG.md which summarizes the entire project and everything we have covered so far. and what are the the next steps in what is to be done.
So far, I have handled this project myself and now, the project will be handled by me and Christa...I have pushed every code to my private github repository and have already invited her to collaborate.

Generate the full DEV_LOG.md in a single file.

Note, The content of the DEV_LOG.md file should contain the maximum details as possible. it should be technical and also understandable (even by someone who does not understand Kotlin or Development well).
It should contain the details of the entire project structure, all the screen and so on. It should be very well detailed.

in the uploaded file, I have the entire structure I got when I used the "tree" command in the terminal.



Worked for 2m 53s

Done — I generated the full DEV_LOG.md as a single detailed handover file.

It includes the project overview, architecture, current source/package structure based on your uploaded tree output, all completed screens/features, Room database progress, receipt flow, dashboard/inventory/settings status, known limitations, GitHub collaboration guidance for you and Christa, and the recommended next steps. The uploaded tree confirms the current package structure under app/src/main/java/com/royalcoffee/pos, including data/local, model, printer, and the UI packages such as dashboard, inventory, login, payment, product, receipt, sales, settings, and theme.

DEV_LOG.md

Library
/
Mobile App Development
/
DEV_LOG.md
Royal Coffee POS — DEV_LOG.md

Project: Royal Coffee POS
Module / Context: Mobile Application Development Coursework / Native Android POS Application
Platform: Android Tablet, Landscape Orientation
Tech Stack: Kotlin, Jetpack Compose, Material 3, Room SQLite, KSP, Android Studio
Current Repository: Private GitHub repository owned by Farhaan; Christa has been invited as collaborator
Current Team Status: Initially developed by Farhaan individually; project is now transitioning to a two-person workflow with Farhaan and Christa
Last Updated: 2026-07-29

1. Purpose of This DEV_LOG

This file documents the full technical progress of the Royal Coffee POS Android project so far. It is intended to act as a handover and continuation guide for both Farhaan and Christa.

The document explains:

What the project is.
What has already been built.
What each screen does.
How the codebase is currently organized.
What major architecture decisions have been made.
Which parts use temporary data and which parts now use Room database.
What is still pending.
How the team should continue working together through GitHub.

The explanations are written to be technical enough for development work, but also understandable for someone who is not yet fully comfortable with Kotlin, Android, or Jetpack Compose.

2. Project Summary

Royal Coffee POS is a native Android tablet Point of Sale application designed for a small café or coffee shop environment. The project focuses on building a realistic POS system that supports sales processing, receipt generation, inventory monitoring, basic dashboard analytics, role-based access, and local database persistence.

The current version is a local-first Android application. It does not yet use a cloud backend for production data synchronization, but the architecture is being prepared so that future features such as cloud backup, Firebase sync, or multi-device support can be added later.

The app is designed primarily for a single Android tablet used in a café. It is locked to landscape orientation because POS systems usually run on wide tablet screens where the product menu, cart, and navigation can be shown side by side.

3. Core Scope of the Application

The locked Version 1 scope currently includes:

3.1 User Roles

There are two user roles:

Admin
Can access Sales.
Can access Dashboard.
Can access Inventory.
Can access Receipt History.
Can access Settings.
Will later manage products, cashier accounts, company settings, and analytics.
Barista / Cashier
Can access Sales.
Can access Settings.
Cannot access Dashboard.
Cannot access Inventory.
Cannot access Receipt History.
Cannot access admin-level controls.
3.2 Main Functional Areas

The app currently covers or has started the following functional areas:

Login screen with role-based access.
Sales / POS ordering screen.
Cart / ticket panel.
Payment method selection.
Cash payment with amount received and change calculation.
Card and mobile payment confirmation flow.
Receipt preview after payment.
Receipt printer service layer placeholder.
ESC/POS-style receipt formatting support.
Room database setup.
Product table and product seeding.
Product data connected to Sales and Inventory screens.
Receipt database tables.
Saving completed receipts to Room.
Receipt History screen.
Receipt detail view design/instructions.
Dashboard with summary cards and Compose-based charts.
Inventory screen with stock monitoring.
Settings screen with role-specific options.
4. Development Status Overview
   Area	Current Status	Notes
   Project setup	Completed	Android Studio project created using Kotlin and Jetpack Compose.
   Landscape orientation	Completed	App is locked to landscape orientation in Android Manifest.
   Theme / branding	Completed for current stage	Royal green and gold theme applied; Cinzel font used for branding.
   Login screen	Completed for current stage	Uses temporary hardcoded credentials. Database-backed users pending.
   Role-based access	Completed for current stage	Admin and Barista see different side navigation options.
   Sales screen	Completed for current stage	Product selection, category filtering, cart, payment, receipt preview.
   Payment flow	Completed for current stage	Cash, Card, and Mobile Payment are available.
   Receipt preview	Completed for current stage	Thermal-style receipt preview with action buttons.
   Printer service	Skeleton implemented	Fake printer layer and ESC/POS formatter exist; real Bluetooth printing pending.
   Main navigation	Completed	Side navigation switches between Sales, Dashboard, Inventory, Receipts, Settings.
   Settings screen	Completed for current stage	Language selection and admin receipt footer editing are in memory for now.
   Inventory screen	Completed for current stage	Reads products/inventory from Room after Step 20.
   Dashboard screen	Completed for current stage	Cards and charts added; some metrics still sample-based.
   Room setup	Completed	Room, KSP, database provider, entities, DAOs, repositories introduced.
   Product database	Completed for current stage	Product table, DAO, repository, ViewModel, seed products.
   Receipts database	Completed structurally	Header/detail tables added.
   Save receipt to Room	Covered / implemented if latest code applied	Next Order saves receipt through ReceiptRepository.
   Receipt History	Covered / implemented if latest code applied	Admin can view saved receipt headers.
   Receipt Detail dialog	Covered / needs verification	Latest next step was to tap a saved receipt and view its line details.
   Cloud backup	Not started	Planned for a future version.
   Real authentication	Not started	PIN hashing and user table pending.
   Product CRUD	Not started	Admin add/edit/delete products pending.
   Inventory stock deduction after sale	Not completed	Products do not yet decrease stock after completed sale.
5. Technology Stack
   5.1 Android and Kotlin

The project is built as a native Android application using:

Kotlin.
Jetpack Compose.
Material 3.
Android Studio.
Room database.
KSP for annotation processing.
5.2 Gradle / Build Configuration

The project uses Kotlin DSL Gradle files:

build.gradle.kts
app/build.gradle.kts
gradle/libs.versions.toml

Known build configuration values used during development:

Android Gradle Plugin: 9.2.1
Kotlin: 2.2.10
KSP: 2.2.10-2.0.2
Compose BOM: 2026.02.01
compileSdk: 37
targetSdk: 36
minSdk: 26
Room: 2.8.4
5.3 Room and KSP Notes

Room was added using:

androidx.room:room-runtime
androidx.room:room-ktx
androidx.room:room-compiler
KSP plugin through alias(libs.plugins.ksp)

A compatibility issue appeared because AGP 9 uses built-in Kotlin and KSP generated source sets. The temporary development fix added to gradle.properties was:

android.disallowKotlinSourceSets=false

This allowed Gradle sync/build to continue while using KSP-generated Room classes.

5.4 Room Migration Note

When new tables were added, the database version was increased. During development, the database provider uses:

.fallbackToDestructiveMigration(dropAllTables = true)

This is acceptable for the current development stage because the app does not yet contain production data. Later, this should be replaced with proper Room migrations so real user data is not lost when schema versions change.

6. Project Structure

The uploaded tree output shows that the project contains the standard Android structure, Gradle folders, generated build folders, source folders, resource folders, and KSP-generated folders.

Important note: folders such as .gradle, .idea, .kotlin, app/build, build, intermediates, and generated are mostly IDE/build-generated folders. These are not usually edited manually. The actual code mainly lives under:

app/src/main/java/com/royalcoffee/pos
app/src/main/res
6.1 Important Source Structure

Current important source package layout:

app/src/main/java/com/royalcoffee/pos
├── data
│   └── local
│       ├── dao
│       ├── database
│       ├── entity
│       └── repository
├── model
├── printer
└── ui
├── components
├── dashboard
├── inventory
├── login
├── payment
├── product
├── receipt
├── sales
├── settings
└── theme
6.2 Package Responsibilities
data/local/entity

Contains Room database table models.

Examples:

ProductEntity.kt
ReceiptHeaderEntity.kt
ReceiptDetailEntity.kt

An entity is a Kotlin class that describes a table in the SQLite database.

data/local/dao

Contains database queries.

Examples:

ProductDao.kt
ReceiptDao.kt

DAO means Data Access Object. It defines the SQL operations Room should run.

data/local/database

Contains the database class and provider.

Examples:

RoyalCoffeeDatabase.kt
RoyalCoffeeDatabaseProvider.kt

This package is responsible for defining the database and creating one shared database instance for the app.

data/local/repository

Contains repository classes.

Examples:

ProductRepository.kt
ReceiptRepository.kt

A repository acts as a clean bridge between the database and the UI/ViewModel layer.

model

Contains general app models that are not necessarily database tables.

Examples:

Product.kt
CartItem.kt
InventoryProduct.kt
Receipt.kt
ReceiptItem.kt
PaymentMethod.kt
UserRole.kt
AppScreen.kt
PrinterConfig.kt
PrinterConnectionType.kt
printer

Contains printing-related service logic.

Examples:

ReceiptPrinter.kt
FakeReceiptPrinter.kt
EscPosReceiptFormatter.kt
BluetoothReceiptPrinter.kt
ReceiptPrintResult.kt
PrinterConnectionStatus.kt
PrinterConnectionCheckResult.kt
ui/login

Contains the login screen.

ui/sales

Contains the Sales / POS screen. At the current stage, this screen also acts as the main app shell because it owns the side navigation and screen switching state.

ui/payment

Contains payment dialogs.

Examples:

PaymentMethodDialog.kt
CashPaymentDialog.kt
ConfirmPaymentDialog.kt
ui/receipt

Contains receipt preview, receipt history, receipt detail models, and receipt history view model.

Examples covered:

ReceiptPreviewDialog.kt
ReceiptHistoryScreen.kt
ReceiptHistoryViewModel.kt
ReceiptHistoryItem.kt
ReceiptDetailItem.kt
ui/dashboard

Contains the dashboard screen and dashboard-specific UI models.

ui/inventory

Contains the inventory screen.

ui/product

Contains ProductViewModel, which connects product Room data to the UI.

ui/settings

Contains the Settings screen.

ui/theme

Contains colors, typography, and theme configuration.

7. Application Flow
   7.1 High-Level Flow
   App starts
   ↓
   Login screen
   ↓
   User enters username and PIN
   ↓
   Role is identified as Admin or Barista
   ↓
   Main POS interface opens
   ↓
   Side navigation shows role-allowed screens
   7.2 Admin Flow

Admin can access:

Sales
Dashboard
Inventory
Receipts
Settings
7.3 Barista Flow

Barista can access:

Sales
Settings

Barista does not see:

Dashboard
Inventory
Receipts
8. Login System
   8.1 Current Login Implementation

The login screen currently uses temporary hardcoded credentials.

Example credentials used during development:

Admin: admin / 1234
Barista: barista / 1234

The login screen determines the user role and passes it into the main app flow.

8.2 Login UI Details

The login screen was improved to be more polished and responsive:

Premium card-style login layout.
Royal Coffee branding.
Coffee cup / RC branding approach.
Cinzel font for the title.
Keyboard behavior improved.
PIN input uses password-style masking.
Landscape/tablet-friendly layout.
Tested on emulator and physical tablet.
8.3 Pending Login Improvements

The login system is not production-ready yet. The following are pending:

Create UserEntity table in Room.
Store admin/barista users locally.
Hash PIN values instead of storing raw PINs.
Add Admin feature to create cashier/barista accounts.
Add account active/inactive status.
Add last login timestamp.
9. Main Navigation
   9.1 Navigation Design

The app uses a left-side vertical navigation rail. This is suitable for tablet landscape POS systems.

The side navigation includes:

RC logo
Sales
Dashboard       Admin only
Inventory       Admin only
Receipts        Admin only
Settings
Logout
9.2 Current Navigation Implementation

Navigation is currently managed using an enum:

enum class AppScreen(
val title: String
) {
SALES("Sales"),
DASHBOARD("Dashboard"),
INVENTORY("Inventory"),
RECEIPTS("Receipts"),
SETTINGS("Settings")
}

The selected screen is stored in Compose state:

var currentScreen by remember {
mutableStateOf(AppScreen.SALES)
}

The app then uses a when (currentScreen) block to decide which screen to show.

9.3 Important Future Refactor

Currently, SalesScreen.kt is doing too much. It contains:

Side navigation.
Sales content.
Ticket/cart logic.
Dialog logic.
Screen switching.
Database repository creation.
ViewModel creation.

This works for the current coursework stage, but a cleaner architecture would eventually separate this into:

MainScreen.kt       → owns side navigation and screen switching
SalesScreen.kt      → only handles sales content
TicketPanel.kt      → separate cart/ticket UI
SideNavigation.kt   → reusable navigation component

This should be done after the major features are stable, not immediately.

10. Sales Screen
    10.1 Purpose

The Sales screen is the main POS screen used by the cashier/barista to process customer orders.

It includes:

Product category chips.
Product grid.
Ticket/cart panel.
Quantity controls.
Order summary.
Clear Ticket button.
Cancel Order button.
Charge button.
Payment dialogs.
Receipt preview.
10.2 Product Display

Originally, product cards contained image placeholders. These were removed to simplify the UI for Version 1.

Current product cards show:

Product name.
Product category.
Product price.

Additional styling added:

Product name uses Cinzel font.
Price uses Cinzel font.
Product card becomes dimmed after payment is confirmed.
10.3 Categories

The categories are dynamically built from product data:

All
Coffee
Pastries
Cold
Hot

The category chips are centered at the top of the Sales screen.

Important technical note: after products were connected to Room, the categories were changed to use remember(products) so that the category list updates when Room finishes loading products.

10.4 Cart / Ticket Panel

The Ticket panel shows:

Ticket title.
Number of items.
Cart items.
Quantity controls.
Subtotal.
Discount.
Tax.
Total.
Payment confirmation summary after payment.

Current tax logic:

Tax = 15% of subtotal
Discount = 0
Total = subtotal - discount + tax

This is simple development logic and can be improved later.

10.5 Clear Ticket

The Clear Ticket button clears the current cart before payment. It is only enabled when there are cart items and payment has not yet been confirmed.

10.6 Cancel Order

The Cancel Order button clears the active order after confirmation.

It clears:

Cart items.
Payment state.
Receipt preview state.
Current receipt object.
Dialog states.

A confirmation dialog is shown to prevent accidental cancellation.

10.7 Order Locking

Once payment is confirmed:

Product cards become dimmed.
Product cards cannot be clicked.
Quantity buttons become disabled.
Cart cannot be changed.
The main button changes from Charge to View Receipt.

This prevents editing a paid order.

11. Payment Flow
    11.1 Payment Method Selection

When the cashier taps Charge, the app opens a payment method dialog.

Available payment methods:

Cash
Card
Mobile Payment

There is no split payment in Version 1.

11.2 Cash Payment

Cash payment asks the cashier to enter amount received.

Logic:

If amount received < total → show validation error
If amount received >= total → confirm payment and calculate change

Example:

Total: Rs 1500
Cash received: Rs 2000
Change: Rs 500
11.3 Card Payment

Card payment is currently simulated.

Flow:

Select Card
Confirm Payment
Receipt preview opens

Real card terminal integration is not included in Version 1.

11.4 Mobile Payment

Mobile Payment is currently simulated.

The current flow does not include QR scanning or QR generation. It simply asks the cashier to confirm that mobile payment was completed.

11.5 Payment Confirmation Behavior

After payment confirmation:

confirmedPaymentMethod is set.
amountPaid is stored.
changeGiven is stored for cash.
Receipt is generated.
Receipt preview opens automatically.
12. Receipt Preview
    12.1 Purpose

Receipt preview allows the cashier/admin to review the final transaction before moving to the next order.

12.2 Receipt Model

A receipt is represented by the app model Receipt.

Important fields include:

Receipt number.
Receipt date/time.
Store name.
Store address lines.
Store phone.
Cashier name.
Receipt items.
Subtotal.
Discount.
Tax.
Total.
Payment method.
Amount paid.
Change given.
Footer message.
12.3 Receipt Items

ReceiptItem is separate from CartItem.

Reason: a cart item is live and editable, while a receipt item should represent the finalized transaction exactly as it happened.

12.4 Receipt Number

Current receipt number generation is in memory:

RC-2026-000001
RC-2026-000002
...

Important limitation: the receipt sequence currently uses an in-memory counter. It may reset when the app restarts. This should be moved to Room later.

12.5 Receipt Footer

The receipt footer is connected to Settings. Admin can change the footer text in the Settings screen, and new generated receipts use that footer.

12.6 Receipt Preview Actions

The receipt preview includes actions such as:

Close.
Download Receipt.
Share via WhatsApp.
Print Receipt.
Next Order.

Current status:

Print uses fake printer service / placeholder result.
Download is not fully implemented.
WhatsApp sharing is not fully implemented.
Next Order is now connected to saving receipts to Room if Step 22 code has been applied.
13. Printer Layer
    13.1 Purpose

The printer layer was introduced to avoid placing printer logic directly inside the UI.

This is good architecture because later the fake printer can be replaced with real Bluetooth printing without rewriting the Sales screen completely.

13.2 Current Printer Files

The printer package includes or has covered:

ReceiptPrinter.kt
FakeReceiptPrinter.kt
EscPosReceiptFormatter.kt
BluetoothReceiptPrinter.kt
ReceiptPrintResult.kt
PrinterConnectionStatus.kt
PrinterConnectionCheckResult.kt
13.3 Fake Printer

The fake printer currently returns a user-friendly message such as:

Printer not available. Check connection.

This avoids misleading the user into thinking a simulated print was successful.

13.4 ESC/POS Formatter

The ESC/POS formatter prepares receipt content in a format closer to what thermal receipt printers use.

Current behavior includes:

Receipt text formatting.
Line dividers.
Header, items, totals, payment info, footer.
Byte array generation for future printer support.
13.5 Bluetooth Printer Skeleton

Bluetooth printing has not been fully implemented yet, but a skeleton exists/planned with:

Bluetooth printer config.
Bluetooth connection status model.
Placeholder connection checks.
Android Bluetooth permission preparation.
13.6 Printer Future Work

Pending:

Scan paired Bluetooth printers.
Allow Admin to select default printer.
Save printer configuration to Room.
Connect to Bluetooth socket.
Send ESC/POS bytes to printer.
Handle printer errors.
Add retry option.
Add reprint receipt from Receipt History.
14. Dashboard Screen
    14.1 Purpose

The Dashboard screen gives Admin a business overview.

It is not visible to Barista users.

14.2 Current Dashboard Features

The dashboard includes:

Today's Sales.
Orders.
Average Order.
Low Stock Count.
Top Products chart.
Payments chart.
Low Stock chart.
14.3 Chart Approach

No external chart library was added. Instead, charts are built using Jetpack Compose layout elements:

Card
Column
Row
Box
Modifier.fillMaxWidth()
weighted widths
rounded bars

This keeps the project simpler and avoids adding unnecessary dependencies.

14.4 Chart Detail Dialogs

Each chart can be tapped to show a structured detail dialog.

Examples:

Top Products chart → Product / Qty Sold / Revenue
Payments chart → Method / Orders / Amount
Low Stock chart → Product / Stock / Reorder Level
14.5 Current Data Source

Current dashboard data is mixed:

Low-stock count comes from Room-backed inventory product data.
Top-selling products still use temporary sample data.
Payment breakdown still uses temporary sample data.
Today's sales, orders, and average order still use temporary sample values.
14.6 Dashboard Next Improvements

Dashboard should later calculate real values from saved receipts:

Today's sales from receipt_headers.total.
Orders count from saved receipt headers.
Average order = total sales / number of orders.
Payment breakdown grouped by payment_method.
Top products grouped from receipt_details.
Dashboard filters for day/week/month.
15. Inventory Screen
    15.1 Purpose

The Inventory screen allows Admin to monitor products and stock levels.

Barista users cannot access Inventory.

15.2 Current Features

Inventory screen shows:

Total product count.
Total stock units.
Low stock count.
Product list.
Product category.
Unit price.
Stock quantity.
Reorder level.
Stock status.
15.3 Room Integration

Inventory originally used sample data. It was later connected to Room through:

ProductEntity
ProductDao
ProductRepository
ProductViewModel

The UI receives data from ProductViewModel.inventoryProducts.

15.4 Current Limitation

Inventory is read-only right now.

Pending features:

Add product.
Edit product.
Disable product.
Delete product if safe.
Update stock quantity.
Stock movement log.
Automatic stock deduction after sale.
Low stock notifications.
16. Settings Screen
    16.1 Purpose

The Settings screen contains basic app preferences.

16.2 Role-Based Settings

Admin can access:

Language selection.
Receipt footer text.

Barista can access:

Language selection only.
16.3 Current Data Storage

Settings are currently stored in Compose memory only.

That means:

Changing language updates the button state.
Changing footer affects new receipts during that app session.
Settings may reset when the app restarts.
16.4 Pending Settings Improvements

Pending:

Create SettingsEntity table.
Save selected language to Room.
Save receipt footer text to Room.
Load settings at app start.
Implement actual English/French text translations.
Add company details table.
Restrict company detail editing based on Version 1 scope.
17. Receipt Database and Receipt History
    17.1 Receipt Database Structure

Receipt saving uses two tables:

receipt_headers
receipt_details

This follows a common POS database pattern.

17.2 Receipt Header

One row in receipt_headers represents one completed sale.

Important fields:

receipt_id
receipt_number
receipt_date_time
cashier_name
subtotal
discount
tax
total
payment_method
amount_paid
change_given
footer_message
17.3 Receipt Detail

One row in receipt_details represents one product line in a receipt.

Important fields:

receipt_detail_id
receipt_id
product_name
quantity
unit_price
line_total
17.4 Foreign Key Relationship

receipt_details.receipt_id references receipt_headers.receipt_id.

This means:

One receipt header can have many receipt detail rows.
17.5 Receipt Saving Flow

Expected receipt saving flow:

Cashier confirms payment
↓
Receipt preview opens
↓
Cashier taps Next Order
↓
ReceiptRepository saves header and details to Room
↓
Toast confirms receipt saved
↓
Current order clears
17.6 Receipt History Screen

Receipt History is Admin-only.

It shows saved receipt headers:

Receipt number.
Date/time.
Cashier.
Payment method.
Total.
17.7 Receipt Detail View

The latest covered next step was to make saved receipts clickable so Admin can view product line details.

Expected behavior:

Admin opens Receipts
↓
Admin taps a saved receipt
↓
Dialog opens
↓
Dialog shows product names, quantities, unit prices, and line totals

This should be verified in the current codebase before moving further.

18. Room Database Architecture
    18.1 Why Room Was Added

Before Room, the app used temporary hardcoded lists such as:

sampleProducts()
sampleInventoryProducts()
sampleTopSellingProducts()
samplePaymentBreakdown()

This was useful for building the UI quickly, but the data disappeared when the app restarted.

Room was added so the app can persist data locally.

18.2 Current Room Components

Current or covered Room components:

ProductEntity
ReceiptHeaderEntity
ReceiptDetailEntity
ProductDao
ReceiptDao
RoyalCoffeeDatabase
RoyalCoffeeDatabaseProvider
ProductRepository
ReceiptRepository
ProductViewModel
ReceiptHistoryViewModel
18.3 Product Data Flow
Room products table
↓
ProductDao
↓
ProductRepository
↓
ProductViewModel
↓
Sales screen and Inventory screen
18.4 Receipt Data Flow
Receipt model from Sales flow
↓
ReceiptRepository
↓
ReceiptDao
↓
receipt_headers and receipt_details tables
↓
ReceiptHistoryViewModel
↓
Receipt History screen
18.5 Seed Products

Default products are seeded only if the product table is empty.

This prevents duplicate products from being inserted every time the app starts.

Example seeded products:

Latte.
Espresso.
Croissant.
Mocha.
Iced Coffee.
Muffin.
Hot Chocolate.
19. Theme and UI Design
    19.1 Branding

The app uses a premium café/royalty-inspired theme:

Royal Green.
Royal Gold.
Coffee Brown.
Soft Cream.
19.2 Typography

Cinzel font was added and used for branding elements such as:

Login title.
Sales title.
Receipt store name.
Product card name/price styling.
Dashboard/Inventory/Settings titles.
19.3 Orientation

The app is landscape-only.

Reason:

POS systems are usually used on tablets in landscape orientation.
Landscape layout allows side navigation, products, and ticket panel to be visible together.
19.4 Responsiveness

The Sales screen uses dynamic sizing through BoxWithConstraints.

Examples:

Side navigation width adjusts based on available width.
Ticket panel width adjusts based on available width.
Product card minimum width adjusts based on tablet screen size.
Safe drawing insets avoid status/navigation bar overlap.
20. Build / Generated Folder Notes

The uploaded project tree includes many generated folders such as:

.gradle
.kotlin
app/build
app/build/generated
app/build/intermediates
app/build/kspCaches
build/reports

These folders are generated by Android Studio, Gradle, Kotlin, Room, and KSP.

Important rule for the team:

Do not manually edit generated build folders.
Do not treat generated build folders as source code.
Do not commit build output folders unless there is a very specific reason.

The most important development folders are:

app/src/main/java
app/src/main/res
gradle/libs.versions.toml
app/build.gradle.kts
build.gradle.kts
gradle.properties
settings.gradle.kts
21. GitHub Collaboration Notes

The project was initially developed by Farhaan alone. Farhaan has pushed all code to a private GitHub repository and invited Christa as a collaborator.

21.1 Recommended Workflow

Recommended team workflow:

main branch       → stable working version
feature branches  → each person works on one feature/fix
pull requests     → review before merging into main
21.2 Suggested Branch Naming

Examples:

feature/receipt-detail-dialog
feature/dashboard-live-data
feature/product-management
feature/settings-room
fix/receipt-save-error
fix/navigation-layout
21.3 Before Starting Work

Each person should run:

git pull

Then create a new branch:

git checkout -b feature/name-of-feature
21.4 Before Committing

Before committing:

Build the project.
Run the app.
Test the affected screen.
Check that no generated build files are accidentally staged.

Commands:

git status
git add .
git commit -m "Meaningful commit message"
git push
21.5 Suggested Commit Style

Use clear commit messages:

Add receipt history screen
Connect product data to Room
Save completed receipts to Room
Add dashboard charts and detail dialogs
Fix dashboard low stock state order
Improve sales product card styling

Avoid vague messages such as:

update
changes
final
stuff
21.6 Suggested Work Division

Farhaan and Christa can divide work like this:

Farhaan
Database architecture.
Room entities, DAOs, repositories.
Sales/payment/receipt logic.
Printer layer.
Dashboard calculations.
Christa
UI polish and screen layout.
Navigation flow documentation.
Wireframes / Figma alignment.
Settings screen improvements.
Receipt History UI and visual testing.
Documentation/screenshots for report.

This division can change, but it is a good starting point.

22. Known Limitations
    22.1 Authentication
    Login is still hardcoded.
    No Room-backed users yet.
    PINs are not hashed yet.
    22.2 Receipt Numbering
    Receipt numbers currently depend on in-memory sequence state.
    Receipt number may reset after app restart.
    Needs database-backed sequence logic.
    22.3 Inventory
    Stock does not automatically decrease after a sale yet.
    Inventory is read-only.
    No product add/edit/delete screen yet.
    22.4 Dashboard
    Some dashboard values are still sample data.
    Dashboard should be connected to receipt database queries.
    22.5 Settings
    Settings are still in memory.
    Language selection does not translate the full app yet.
    Receipt footer is not persisted yet unless later connected to Room.
    22.6 Printing
    Real Bluetooth printing is not implemented yet.
    Current printer returns placeholder error/result.
    22.7 Cloud Sync
    Cloud backup/sync is not implemented.
    Firebase is planned for future version, not current core version.
    22.8 Testing
    No full automated test suite yet.
    Most testing is currently manual.
23. Manual Testing Checklist
    23.1 Login Tests
    Admin login works.
    Barista login works.
    Wrong PIN shows an error.
    Logout returns user to login.
    23.2 Role-Based Navigation Tests
    Admin sees Sales, Dashboard, Inventory, Receipts, Settings.
    Barista sees Sales and Settings only.
    Admin screens are hidden from Barista.
    Selected navigation item is highlighted.
    23.3 Sales Tests
    Products load from Room.
    Categories appear correctly.
    Category filtering works.
    Product card tap adds item to cart.
    Tapping same product increases quantity.
    Quantity + button works.
    Quantity - button works.
    Quantity reaching zero removes item.
    Clear Ticket works before payment.
    Cancel Order opens confirmation.
    Cancel Order clears active order.
    23.4 Payment Tests
    Charge button is disabled when cart is empty.
    Charge button opens payment method dialog.
    Cash payment validates amount received.
    Cash payment calculates change.
    Card payment confirms successfully.
    Mobile payment confirms successfully.
    Receipt preview opens after payment.
    Paid order cannot be edited.
    23.5 Receipt Tests
    Receipt preview shows correct products.
    Receipt preview shows correct subtotal/tax/total.
    Receipt preview shows payment method.
    Receipt preview shows amount paid and change for cash.
    Receipt footer reflects admin settings for current session.
    Print button shows printer availability message.
    Next Order saves receipt to Room.
    Next Order clears current order.
    23.6 Receipt History Tests
    Admin can open Receipts screen.
    Saved receipts appear after completing orders.
    Receipt number/date/payment/total are shown.
    Tapping a receipt opens detail dialog if Step 24 is implemented.
    Receipt detail dialog shows product lines.
    Barista cannot see Receipts screen.
    23.7 Dashboard Tests
    Dashboard opens for Admin.
    Barista cannot access Dashboard.
    Summary cards appear.
    Charts appear.
    Tap Top Products chart opens details.
    Tap Payments chart opens details.
    Tap Low Stock chart opens details.
    Low stock count matches inventory low stock items.
    23.8 Inventory Tests
    Inventory opens for Admin.
    Barista cannot access Inventory.
    Product list appears.
    Low stock items are highlighted.
    Stock quantities and reorder levels are visible.
    23.9 Settings Tests
    Admin sees language and receipt footer settings.
    Barista sees language only.
    Admin footer change affects new receipt preview.
    Language buttons visually toggle.
24. Recommended Next Steps

This section lists the best order to continue development after this pause.

Step 1 — Verify Latest Receipt Detail Implementation

Check whether the latest receipt detail feature has been fully applied.

Verify:

ReceiptDetailItem.kt exists.
ReceiptDao.getReceiptDetailsOnce() exists.
ReceiptRepository.getReceiptDetailsOnce() exists.
ReceiptHistoryViewModel has selected receipt state.
ReceiptHistoryScreen supports tapping a receipt.
SalesScreen.kt passes detail state/actions into ReceiptHistoryScreen.

Then test:

Create order → save receipt → open Receipts → tap receipt → view product lines.
Step 2 — Make Dashboard Use Real Receipt Data

Replace sample dashboard values with Room queries.

Needed queries:

Total sales.
Number of orders.
Average order value.
Payment breakdown grouped by method.
Top products grouped by product name.

Likely DAO additions:

SELECT SUM(total) FROM receipt_headers
SELECT COUNT(*) FROM receipt_headers
SELECT payment_method, COUNT(*), SUM(total) FROM receipt_headers GROUP BY payment_method
SELECT product_name, SUM(quantity), SUM(line_total) FROM receipt_details GROUP BY product_name
Step 3 — Fix Receipt Numbering Permanently

Move receipt sequence generation from memory to database.

Options:

Generate receipt number after insert using receipt ID.
Store last receipt sequence in Settings table.
Query latest receipt count/ID and generate next number.

Recommended simple approach:

Use receipt ID or count from database to generate next receipt number.
Step 4 — Deduct Inventory Stock After Sale

When receipt is saved, stock quantity should decrease.

Example:

Before sale: Latte stock = 28
Sale: 2 Latte
After sale: Latte stock = 26

This will require product ID tracking in receipt details or mapping product names carefully.

Better future design: receipt details should store product_id as well as product_name.

Step 5 — Add Admin Product Management

Inventory should become editable.

Admin should be able to:

Add product.
Edit product name/category/price/stock/reorder level.
Disable product.
Possibly delete product if no sales history exists.
Step 6 — Save Settings to Room

Create SettingsEntity.

Save:

Selected language.
Receipt footer text.
Possibly company details later.
Step 7 — Add Room-Backed Users

Create:

UserEntity
UserDao
UserRepository
LoginViewModel

Support:

Admin default user seeding.
Barista account creation.
PIN hash.
Role.
Active/inactive status.
Step 8 — Add Company Table

Create CompanyEntity.

Store:

Company name.
Address lines.
Country.
BRN.
VAT.
Phone.
Email.

Current Version 1 decision: company details are not editable from Settings, but should still exist in the database later.

Step 9 — Improve Receipt History

Add:

Search by receipt number.
Filter by date.
Filter by payment method.
Reprint receipt.
View full receipt preview from saved data.
Export receipt PDF.
Step 10 — Real Printer Integration

Implement:

Bluetooth device discovery/selection.
Save selected printer.
Connect to paired printer.
Send ESC/POS bytes.
Handle printer unavailable errors.
Step 11 — Improve Architecture

Refactor large files into smaller components.

Recommended:

MainScreen.kt
SideNavigation.kt
SalesScreen.kt
ProductArea.kt
TicketPanel.kt
DashboardScreen.kt
InventoryScreen.kt
ReceiptHistoryScreen.kt
SettingsScreen.kt
Step 12 — Documentation and Report Updates

Update coursework report with:

Current architecture.
Room database design.
Screenshots.
Role-based navigation.
Receipt saving workflow.
Limitations.
Future work.
25. Suggested Current Milestone Commit Tags

Useful commit checkpoints already covered or recommended:

Add payment screens for cash card and mobile
Improve login screen responsiveness and keyboard handling
Improve ticket panel UI design
Improve payment method dialog UI
Add generate receipt action after payment confirmation
Add receipt preview dialog and next order flow
Add dynamic receipt number and timestamp
Refactor receipt preview to use receipt model
Improve thermal receipt preview action layout
Add fake receipt printer service layer
Add ESC POS receipt formatter
Add Bluetooth printer support skeleton
Add main screen navigation
Add settings screen
Add inventory screen
Add dashboard screen
Add dashboard charts and detail dialogs
Add Room database dependencies
Add product database table
Add product database provider and repository
Connect product data to Room
Add receipt database tables
Save completed receipts to Room
Add receipt history screen
Add receipt detail view
26. Glossary for Non-Developers
    Kotlin

The programming language used to build the Android app.

Jetpack Compose

Android's modern UI toolkit. Instead of XML layouts, screens are built using Kotlin functions called Composables.

Composable

A function that draws part of the UI. Example: a button, card, screen, or dialog.

Room

Android library that makes it easier to use SQLite databases.

SQLite

A local database stored on the device.

Entity

A Kotlin class that represents a database table.

DAO

Data Access Object. Contains database queries.

Repository

A clean layer between the database and the rest of the app.

ViewModel

A class that holds UI data and business logic so the UI does not directly manage everything.

Flow

A Kotlin data stream. When database data changes, the UI can automatically receive updates.

StateFlow

A Flow that always has a current value and is useful for UI state.

KSP

Kotlin Symbol Processing. Used by Room to generate database code automatically.

ESC/POS

A command format used by many thermal receipt printers.

Destructive Migration

A development shortcut where Room deletes and recreates tables when the database structure changes. Useful during development, unsafe for production data.

27. Final Handover Notes

The project is currently in a strong intermediate state. The UI is already functional, and the app has moved from pure sample data toward a real Room-backed architecture.

The most important thing now is to avoid rushing into many new features at once. The recommended path is:

1. Verify receipt saving and receipt detail view.
2. Make dashboard use real receipt data.
3. Fix receipt numbering using database state.
4. Deduct stock after completed sale.
5. Add product management.
6. Add user management.
7. Save settings permanently.
8. Refactor large UI files.
9. Polish and document for submission.

Farhaan and Christa should coordinate through GitHub using feature branches and clear commit messages. Before making major structural changes, both collaborators should pull the latest code and agree on who is editing which package/screen.

This DEV_LOG should be updated after every major feature or debugging checkpoint.




Library
/
Mobile App Development
/
DEV_LOG.md
Royal Coffee POS — DEV_LOG.md

Project: Royal Coffee POS
Module / Context: Mobile Application Development Coursework / Native Android POS Application
Platform: Android Tablet, Landscape Orientation
Tech Stack: Kotlin, Jetpack Compose, Material 3, Room SQLite, KSP, Android Studio
Current Repository: Private GitHub repository owned by Farhaan; Christa has been invited as collaborator
Current Team Status: Initially developed by Farhaan individually; project is now transitioning to a two-person workflow with Farhaan and Christa
Last Updated: 2026-07-29

1. Purpose of This DEV_LOG

This file documents the full technical progress of the Royal Coffee POS Android project so far. It is intended to act as a handover and continuation guide for both Farhaan and Christa.

The document explains:

What the project is.
What has already been built.
What each screen does.
How the codebase is currently organized.
What major architecture decisions have been made.
Which parts use temporary data and which parts now use Room database.
What is still pending.
How the team should continue working together through GitHub.

The explanations are written to be technical enough for development work, but also understandable for someone who is not yet fully comfortable with Kotlin, Android, or Jetpack Compose.

2. Project Summary

Royal Coffee POS is a native Android tablet Point of Sale application designed for a small café or coffee shop environment. The project focuses on building a realistic POS system that supports sales processing, receipt generation, inventory monitoring, basic dashboard analytics, role-based access, and local database persistence.

The current version is a local-first Android application. It does not yet use a cloud backend for production data synchronization, but the architecture is being prepared so that future features such as cloud backup, Firebase sync, or multi-device support can be added later.

The app is designed primarily for a single Android tablet used in a café. It is locked to landscape orientation because POS systems usually run on wide tablet screens where the product menu, cart, and navigation can be shown side by side.

3. Core Scope of the Application

The locked Version 1 scope currently includes:

3.1 User Roles

There are two user roles:

Admin
Can access Sales.
Can access Dashboard.
Can access Inventory.
Can access Receipt History.
Can access Settings.
Will later manage products, cashier accounts, company settings, and analytics.
Barista / Cashier
Can access Sales.
Can access Settings.
Cannot access Dashboard.
Cannot access Inventory.
Cannot access Receipt History.
Cannot access admin-level controls.
3.2 Main Functional Areas

The app currently covers or has started the following functional areas:

Login screen with role-based access.
Sales / POS ordering screen.
Cart / ticket panel.
Payment method selection.
Cash payment with amount received and change calculation.
Card and mobile payment confirmation flow.
Receipt preview after payment.
Receipt printer service layer placeholder.
ESC/POS-style receipt formatting support.
Room database setup.
Product table and product seeding.
Product data connected to Sales and Inventory screens.
Receipt database tables.
Saving completed receipts to Room.
Receipt History screen.
Receipt detail view design/instructions.
Dashboard with summary cards and Compose-based charts.
Inventory screen with stock monitoring.
Settings screen with role-specific options.
4. Development Status Overview
   Area	Current Status	Notes
   Project setup	Completed	Android Studio project created using Kotlin and Jetpack Compose.
   Landscape orientation	Completed	App is locked to landscape orientation in Android Manifest.
   Theme / branding	Completed for current stage	Royal green and gold theme applied; Cinzel font used for branding.
   Login screen	Completed for current stage	Uses temporary hardcoded credentials. Database-backed users pending.
   Role-based access	Completed for current stage	Admin and Barista see different side navigation options.
   Sales screen	Completed for current stage	Product selection, category filtering, cart, payment, receipt preview.
   Payment flow	Completed for current stage	Cash, Card, and Mobile Payment are available.
   Receipt preview	Completed for current stage	Thermal-style receipt preview with action buttons.
   Printer service	Skeleton implemented	Fake printer layer and ESC/POS formatter exist; real Bluetooth printing pending.
   Main navigation	Completed	Side navigation switches between Sales, Dashboard, Inventory, Receipts, Settings.
   Settings screen	Completed for current stage	Language selection and admin receipt footer editing are in memory for now.
   Inventory screen	Completed for current stage	Reads products/inventory from Room after Step 20.
   Dashboard screen	Completed for current stage	Cards and charts added; some metrics still sample-based.
   Room setup	Completed	Room, KSP, database provider, entities, DAOs, repositories introduced.
   Product database	Completed for current stage	Product table, DAO, repository, ViewModel, seed products.
   Receipts database	Completed structurally	Header/detail tables added.
   Save receipt to Room	Covered / implemented if latest code applied	Next Order saves receipt through ReceiptRepository.
   Receipt History	Covered / implemented if latest code applied	Admin can view saved receipt headers.
   Receipt Detail dialog	Covered / needs verification	Latest next step was to tap a saved receipt and view its line details.
   Cloud backup	Not started	Planned for a future version.
   Real authentication	Not started	PIN hashing and user table pending.
   Product CRUD	Not started	Admin add/edit/delete products pending.
   Inventory stock deduction after sale	Not completed	Products do not yet decrease stock after completed sale.
5. Technology Stack
   5.1 Android and Kotlin

The project is built as a native Android application using:

Kotlin.
Jetpack Compose.
Material 3.
Android Studio.
Room database.
KSP for annotation processing.
5.2 Gradle / Build Configuration

The project uses Kotlin DSL Gradle files:

build.gradle.kts
app/build.gradle.kts
gradle/libs.versions.toml

Known build configuration values used during development:

Android Gradle Plugin: 9.2.1
Kotlin: 2.2.10
KSP: 2.2.10-2.0.2
Compose BOM: 2026.02.01
compileSdk: 37
targetSdk: 36
minSdk: 26
Room: 2.8.4
5.3 Room and KSP Notes

Room was added using:

androidx.room:room-runtime
androidx.room:room-ktx
androidx.room:room-compiler
KSP plugin through alias(libs.plugins.ksp)

A compatibility issue appeared because AGP 9 uses built-in Kotlin and KSP generated source sets. The temporary development fix added to gradle.properties was:

android.disallowKotlinSourceSets=false

This allowed Gradle sync/build to continue while using KSP-generated Room classes.

5.4 Room Migration Note

When new tables were added, the database version was increased. During development, the database provider uses:

.fallbackToDestructiveMigration(dropAllTables = true)

This is acceptable for the current development stage because the app does not yet contain production data. Later, this should be replaced with proper Room migrations so real user data is not lost when schema versions change.

6. Project Structure

The uploaded tree output shows that the project contains the standard Android structure, Gradle folders, generated build folders, source folders, resource folders, and KSP-generated folders.

Important note: folders such as .gradle, .idea, .kotlin, app/build, build, intermediates, and generated are mostly IDE/build-generated folders. These are not usually edited manually. The actual code mainly lives under:

app/src/main/java/com/royalcoffee/pos
app/src/main/res
6.1 Important Source Structure

Current important source package layout:

app/src/main/java/com/royalcoffee/pos
├── data
│   └── local
│       ├── dao
│       ├── database
│       ├── entity
│       └── repository
├── model
├── printer
└── ui
├── components
├── dashboard
├── inventory
├── login
├── payment
├── product
├── receipt
├── sales
├── settings
└── theme
6.2 Package Responsibilities
data/local/entity

Contains Room database table models.

Examples:

ProductEntity.kt
ReceiptHeaderEntity.kt
ReceiptDetailEntity.kt

An entity is a Kotlin class that describes a table in the SQLite database.

data/local/dao

Contains database queries.

Examples:

ProductDao.kt
ReceiptDao.kt

DAO means Data Access Object. It defines the SQL operations Room should run.

data/local/database

Contains the database class and provider.

Examples:

RoyalCoffeeDatabase.kt
RoyalCoffeeDatabaseProvider.kt

This package is responsible for defining the database and creating one shared database instance for the app.

data/local/repository

Contains repository classes.

Examples:

ProductRepository.kt
ReceiptRepository.kt

A repository acts as a clean bridge between the database and the UI/ViewModel layer.

model

Contains general app models that are not necessarily database tables.

Examples:

Product.kt
CartItem.kt
InventoryProduct.kt
Receipt.kt
ReceiptItem.kt
PaymentMethod.kt
UserRole.kt
AppScreen.kt
PrinterConfig.kt
PrinterConnectionType.kt
printer

Contains printing-related service logic.

Examples:

ReceiptPrinter.kt
FakeReceiptPrinter.kt
EscPosReceiptFormatter.kt
BluetoothReceiptPrinter.kt
ReceiptPrintResult.kt
PrinterConnectionStatus.kt
PrinterConnectionCheckResult.kt
ui/login

Contains the login screen.

ui/sales

Contains the Sales / POS screen. At the current stage, this screen also acts as the main app shell because it owns the side navigation and screen switching state.

ui/payment

Contains payment dialogs.

Examples:

PaymentMethodDialog.kt
CashPaymentDialog.kt
ConfirmPaymentDialog.kt
ui/receipt

Contains receipt preview, receipt history, receipt detail models, and receipt history view model.

Examples covered:

ReceiptPreviewDialog.kt
ReceiptHistoryScreen.kt
ReceiptHistoryViewModel.kt
ReceiptHistoryItem.kt
ReceiptDetailItem.kt
ui/dashboard

Contains the dashboard screen and dashboard-specific UI models.

ui/inventory

Contains the inventory screen.

ui/product

Contains ProductViewModel, which connects product Room data to the UI.

ui/settings

Contains the Settings screen.

ui/theme

Contains colors, typography, and theme configuration.

7. Application Flow
   7.1 High-Level Flow
   App starts
   ↓
   Login screen
   ↓
   User enters username and PIN
   ↓
   Role is identified as Admin or Barista
   ↓
   Main POS interface opens
   ↓
   Side navigation shows role-allowed screens
   7.2 Admin Flow

Admin can access:

Sales
Dashboard
Inventory
Receipts
Settings
7.3 Barista Flow

Barista can access:

Sales
Settings

Barista does not see:

Dashboard
Inventory
Receipts
8. Login System
   8.1 Current Login Implementation

The login screen currently uses temporary hardcoded credentials.

Example credentials used during development:

Admin: admin / 1234
Barista: barista / 1234

The login screen determines the user role and passes it into the main app flow.

8.2 Login UI Details

The login screen was improved to be more polished and responsive:

Premium card-style login layout.
Royal Coffee branding.
Coffee cup / RC branding approach.
Cinzel font for the title.
Keyboard behavior improved.
PIN input uses password-style masking.
Landscape/tablet-friendly layout.
Tested on emulator and physical tablet.
8.3 Pending Login Improvements

The login system is not production-ready yet. The following are pending:

Create UserEntity table in Room.
Store admin/barista users locally.
Hash PIN values instead of storing raw PINs.
Add Admin feature to create cashier/barista accounts.
Add account active/inactive status.
Add last login timestamp.
9. Main Navigation
   9.1 Navigation Design

The app uses a left-side vertical navigation rail. This is suitable for tablet landscape POS systems.

The side navigation includes:

RC logo
Sales
Dashboard       Admin only
Inventory       Admin only
Receipts        Admin only
Settings
Logout
9.2 Current Navigation Implementation

Navigation is currently managed using an enum:

enum class AppScreen(
val title: String
) {
SALES("Sales"),
DASHBOARD("Dashboard"),
INVENTORY("Inventory"),
RECEIPTS("Receipts"),
SETTINGS("Settings")
}

The selected screen is stored in Compose state:

var currentScreen by remember {
mutableStateOf(AppScreen.SALES)
}

The app then uses a when (currentScreen) block to decide which screen to show.

9.3 Important Future Refactor

Currently, SalesScreen.kt is doing too much. It contains:

Side navigation.
Sales content.
Ticket/cart logic.
Dialog logic.
Screen switching.
Database repository creation.
ViewModel creation.

This works for the current coursework stage, but a cleaner architecture would eventually separate this into:

MainScreen.kt       → owns side navigation and screen switching
SalesScreen.kt      → only handles sales content
TicketPanel.kt      → separate cart/ticket UI
SideNavigation.kt   → reusable navigation component

This should be done after the major features are stable, not immediately.

10. Sales Screen
    10.1 Purpose

The Sales screen is the main POS screen used by the cashier/barista to process customer orders.

It includes:

Product category chips.
Product grid.
Ticket/cart panel.
Quantity controls.
Order summary.
Clear Ticket button.
Cancel Order button.
Charge button.
Payment dialogs.
Receipt preview.
10.2 Product Display

Originally, product cards contained image placeholders. These were removed to simplify the UI for Version 1.

Current product cards show:

Product name.
Product category.
Product price.

Additional styling added:

Product name uses Cinzel font.
Price uses Cinzel font.
Product card becomes dimmed after payment is confirmed.
10.3 Categories

The categories are dynamically built from product data:

All
Coffee
Pastries
Cold
Hot

The category chips are centered at the top of the Sales screen.

Important technical note: after products were connected to Room, the categories were changed to use remember(products) so that the category list updates when Room finishes loading products.

10.4 Cart / Ticket Panel

The Ticket panel shows:

Ticket title.
Number of items.
Cart items.
Quantity controls.
Subtotal.
Discount.
Tax.
Total.
Payment confirmation summary after payment.

Current tax logic:

Tax = 15% of subtotal
Discount = 0
Total = subtotal - discount + tax

This is simple development logic and can be improved later.

10.5 Clear Ticket

The Clear Ticket button clears the current cart before payment. It is only enabled when there are cart items and payment has not yet been confirmed.

10.6 Cancel Order

The Cancel Order button clears the active order after confirmation.

It clears:

Cart items.
Payment state.
Receipt preview state.
Current receipt object.
Dialog states.

A confirmation dialog is shown to prevent accidental cancellation.

10.7 Order Locking

Once payment is confirmed:

Product cards become dimmed.
Product cards cannot be clicked.
Quantity buttons become disabled.
Cart cannot be changed.
The main button changes from Charge to View Receipt.

This prevents editing a paid order.

11. Payment Flow
    11.1 Payment Method Selection

When the cashier taps Charge, the app opens a payment method dialog.

Available payment methods:

Cash
Card
Mobile Payment

There is no split payment in Version 1.

11.2 Cash Payment

Cash payment asks the cashier to enter amount received.

Logic:

If amount received < total → show validation error
If amount received >= total → confirm payment and calculate change

Example:

Total: Rs 1500
Cash received: Rs 2000
Change: Rs 500
11.3 Card Payment

Card payment is currently simulated.

Flow:

Select Card
Confirm Payment
Receipt preview opens

Real card terminal integration is not included in Version 1.

11.4 Mobile Payment

Mobile Payment is currently simulated.

The current flow does not include QR scanning or QR generation. It simply asks the cashier to confirm that mobile payment was completed.

11.5 Payment Confirmation Behavior

After payment confirmation:

confirmedPaymentMethod is set.
amountPaid is stored.
changeGiven is stored for cash.
Receipt is generated.
Receipt preview opens automatically.
12. Receipt Preview
    12.1 Purpose

Receipt preview allows the cashier/admin to review the final transaction before moving to the next order.

12.2 Receipt Model

A receipt is represented by the app model Receipt.

Important fields include:

Receipt number.
Receipt date/time.
Store name.
Store address lines.
Store phone.
Cashier name.
Receipt items.
Subtotal.
Discount.
Tax.
Total.
Payment method.
Amount paid.
Change given.
Footer message.
12.3 Receipt Items

ReceiptItem is separate from CartItem.

Reason: a cart item is live and editable, while a receipt item should represent the finalized transaction exactly as it happened.

12.4 Receipt Number

Current receipt number generation is in memory:

RC-2026-000001
RC-2026-000002
...

Important limitation: the receipt sequence currently uses an in-memory counter. It may reset when the app restarts. This should be moved to Room later.

12.5 Receipt Footer

The receipt footer is connected to Settings. Admin can change the footer text in the Settings screen, and new generated receipts use that footer.

12.6 Receipt Preview Actions

The receipt preview includes actions such as:

Close.
Download Receipt.
Share via WhatsApp.
Print Receipt.
Next Order.

Current status:

Print uses fake printer service / placeholder result.
Download is not fully implemented.
WhatsApp sharing is not fully implemented.
Next Order is now connected to saving receipts to Room if Step 22 code has been applied.
13. Printer Layer
    13.1 Purpose

The printer layer was introduced to avoid placing printer logic directly inside the UI.

This is good architecture because later the fake printer can be replaced with real Bluetooth printing without rewriting the Sales screen completely.

13.2 Current Printer Files

The printer package includes or has covered:

ReceiptPrinter.kt
FakeReceiptPrinter.kt
EscPosReceiptFormatter.kt
BluetoothReceiptPrinter.kt
ReceiptPrintResult.kt
PrinterConnectionStatus.kt
PrinterConnectionCheckResult.kt
13.3 Fake Printer

The fake printer currently returns a user-friendly message such as:

Printer not available. Check connection.

This avoids misleading the user into thinking a simulated print was successful.

13.4 ESC/POS Formatter

The ESC/POS formatter prepares receipt content in a format closer to what thermal receipt printers use.

Current behavior includes:

Receipt text formatting.
Line dividers.
Header, items, totals, payment info, footer.
Byte array generation for future printer support.
13.5 Bluetooth Printer Skeleton

Bluetooth printing has not been fully implemented yet, but a skeleton exists/planned with:

Bluetooth printer config.
Bluetooth connection status model.
Placeholder connection checks.
Android Bluetooth permission preparation.
13.6 Printer Future Work

Pending:

Scan paired Bluetooth printers.
Allow Admin to select default printer.
Save printer configuration to Room.
Connect to Bluetooth socket.
Send ESC/POS bytes to printer.
Handle printer errors.
Add retry option.
Add reprint receipt from Receipt History.
14. Dashboard Screen
    14.1 Purpose

The Dashboard screen gives Admin a business overview.

It is not visible to Barista users.

14.2 Current Dashboard Features

The dashboard includes:

Today's Sales.
Orders.
Average Order.
Low Stock Count.
Top Products chart.
Payments chart.
Low Stock chart.
14.3 Chart Approach

No external chart library was added. Instead, charts are built using Jetpack Compose layout elements:

Card
Column
Row
Box
Modifier.fillMaxWidth()
weighted widths
rounded bars

This keeps the project simpler and avoids adding unnecessary dependencies.

14.4 Chart Detail Dialogs

Each chart can be tapped to show a structured detail dialog.

Examples:

Top Products chart → Product / Qty Sold / Revenue
Payments chart → Method / Orders / Amount
Low Stock chart → Product / Stock / Reorder Level
14.5 Current Data Source

Current dashboard data is mixed:

Low-stock count comes from Room-backed inventory product data.
Top-selling products still use temporary sample data.
Payment breakdown still uses temporary sample data.
Today's sales, orders, and average order still use temporary sample values.
14.6 Dashboard Next Improvements

Dashboard should later calculate real values from saved receipts:

Today's sales from receipt_headers.total.
Orders count from saved receipt headers.
Average order = total sales / number of orders.
Payment breakdown grouped by payment_method.
Top products grouped from receipt_details.
Dashboard filters for day/week/month.
15. Inventory Screen
    15.1 Purpose

The Inventory screen allows Admin to monitor products and stock levels.

Barista users cannot access Inventory.

15.2 Current Features

Inventory screen shows:

Total product count.
Total stock units.
Low stock count.
Product list.
Product category.
Unit price.
Stock quantity.
Reorder level.
Stock status.
15.3 Room Integration

Inventory originally used sample data. It was later connected to Room through:

ProductEntity
ProductDao
ProductRepository
ProductViewModel

The UI receives data from ProductViewModel.inventoryProducts.

15.4 Current Limitation

Inventory is read-only right now.

Pending features:

Add product.
Edit product.
Disable product.
Delete product if safe.
Update stock quantity.
Stock movement log.
Automatic stock deduction after sale.
Low stock notifications.
16. Settings Screen
    16.1 Purpose

The Settings screen contains basic app preferences.

16.2 Role-Based Settings

Admin can access:

Language selection.
Receipt footer text.

Barista can access:

Language selection only.
16.3 Current Data Storage

Settings are currently stored in Compose memory only.

That means:

Changing language updates the button state.
Changing footer affects new receipts during that app session.
Settings may reset when the app restarts.
16.4 Pending Settings Improvements

Pending:

Create SettingsEntity table.
Save selected language to Room.
Save receipt footer text to Room.
Load settings at app start.
Implement actual English/French text translations.
Add company details table.
Restrict company detail editing based on Version 1 scope.
17. Receipt Database and Receipt History
    17.1 Receipt Database Structure

Receipt saving uses two tables:

receipt_headers
receipt_details

This follows a common POS database pattern.

17.2 Receipt Header

One row in receipt_headers represents one completed sale.

Important fields:

receipt_id
receipt_number
receipt_date_time
cashier_name
subtotal
discount
tax
total
payment_method
amount_paid
change_given
footer_message
17.3 Receipt Detail

One row in receipt_details represents one product line in a receipt.

Important fields:

receipt_detail_id
receipt_id
product_name
quantity
unit_price
line_total
17.4 Foreign Key Relationship

receipt_details.receipt_id references receipt_headers.receipt_id.

This means:

One receipt header can have many receipt detail rows.
17.5 Receipt Saving Flow

Expected receipt saving flow:

Cashier confirms payment
↓
Receipt preview opens
↓
Cashier taps Next Order
↓
ReceiptRepository saves header and details to Room
↓
Toast confirms receipt saved
↓
Current order clears
17.6 Receipt History Screen

Receipt History is Admin-only.

It shows saved receipt headers:

Receipt number.
Date/time.
Cashier.
Payment method.
Total.
17.7 Receipt Detail View

The latest covered next step was to make saved receipts clickable so Admin can view product line details.

Expected behavior:

Admin opens Receipts
↓
Admin taps a saved receipt
↓
Dialog opens
↓
Dialog shows product names, quantities, unit prices, and line totals

This should be verified in the current codebase before moving further.

18. Room Database Architecture
    18.1 Why Room Was Added

Before Room, the app used temporary hardcoded lists such as:

sampleProducts()
sampleInventoryProducts()
sampleTopSellingProducts()
samplePaymentBreakdown()

This was useful for building the UI quickly, but the data disappeared when the app restarted.

Room was added so the app can persist data locally.

18.2 Current Room Components

Current or covered Room components:

ProductEntity
ReceiptHeaderEntity
ReceiptDetailEntity
ProductDao
ReceiptDao
RoyalCoffeeDatabase
RoyalCoffeeDatabaseProvider
ProductRepository
ReceiptRepository
ProductViewModel
ReceiptHistoryViewModel
18.3 Product Data Flow
Room products table
↓
ProductDao
↓
ProductRepository
↓
ProductViewModel
↓
Sales screen and Inventory screen
18.4 Receipt Data Flow
Receipt model from Sales flow
↓
ReceiptRepository
↓
ReceiptDao
↓
receipt_headers and receipt_details tables
↓
ReceiptHistoryViewModel
↓
Receipt History screen
18.5 Seed Products

Default products are seeded only if the product table is empty.

This prevents duplicate products from being inserted every time the app starts.

Example seeded products:

Latte.
Espresso.
Croissant.
Mocha.
Iced Coffee.
Muffin.
Hot Chocolate.
19. Theme and UI Design
    19.1 Branding

The app uses a premium café/royalty-inspired theme:

Royal Green.
Royal Gold.
Coffee Brown.
Soft Cream.
19.2 Typography

Cinzel font was added and used for branding elements such as:

Login title.
Sales title.
Receipt store name.
Product card name/price styling.
Dashboard/Inventory/Settings titles.
19.3 Orientation

The app is landscape-only.

Reason:

POS systems are usually used on tablets in landscape orientation.
Landscape layout allows side navigation, products, and ticket panel to be visible together.
19.4 Responsiveness

The Sales screen uses dynamic sizing through BoxWithConstraints.

Examples:

Side navigation width adjusts based on available width.
Ticket panel width adjusts based on available width.
Product card minimum width adjusts based on tablet screen size.
Safe drawing insets avoid status/navigation bar overlap.
20. Build / Generated Folder Notes

The uploaded project tree includes many generated folders such as:

.gradle
.kotlin
app/build
app/build/generated
app/build/intermediates
app/build/kspCaches
build/reports

These folders are generated by Android Studio, Gradle, Kotlin, Room, and KSP.

Important rule for the team:

Do not manually edit generated build folders.
Do not treat generated build folders as source code.
Do not commit build output folders unless there is a very specific reason.

The most important development folders are:

app/src/main/java
app/src/main/res
gradle/libs.versions.toml
app/build.gradle.kts
build.gradle.kts
gradle.properties
settings.gradle.kts
21. GitHub Collaboration Notes

The project was initially developed by Farhaan alone. Farhaan has pushed all code to a private GitHub repository and invited Christa as a collaborator.

21.1 Recommended Workflow

Recommended team workflow:

main branch       → stable working version
feature branches  → each person works on one feature/fix
pull requests     → review before merging into main
21.2 Suggested Branch Naming

Examples:

feature/receipt-detail-dialog
feature/dashboard-live-data
feature/product-management
feature/settings-room
fix/receipt-save-error
fix/navigation-layout
21.3 Before Starting Work

Each person should run:

git pull

Then create a new branch:

git checkout -b feature/name-of-feature
21.4 Before Committing

Before committing:

Build the project.
Run the app.
Test the affected screen.
Check that no generated build files are accidentally staged.

Commands:

git status
git add .
git commit -m "Meaningful commit message"
git push
21.5 Suggested Commit Style

Use clear commit messages:

Add receipt history screen
Connect product data to Room
Save completed receipts to Room
Add dashboard charts and detail dialogs
Fix dashboard low stock state order
Improve sales product card styling

Avoid vague messages such as:

update
changes
final
stuff
21.6 Suggested Work Division

Farhaan and Christa can divide work like this:

Farhaan
Database architecture.
Room entities, DAOs, repositories.
Sales/payment/receipt logic.
Printer layer.
Dashboard calculations.
Christa
UI polish and screen layout.
Navigation flow documentation.
Wireframes / Figma alignment.
Settings screen improvements.
Receipt History UI and visual testing.
Documentation/screenshots for report.

This division can change, but it is a good starting point.

22. Known Limitations
    22.1 Authentication
    Login is still hardcoded.
    No Room-backed users yet.
    PINs are not hashed yet.
    22.2 Receipt Numbering
    Receipt numbers currently depend on in-memory sequence state.
    Receipt number may reset after app restart.
    Needs database-backed sequence logic.
    22.3 Inventory
    Stock does not automatically decrease after a sale yet.
    Inventory is read-only.
    No product add/edit/delete screen yet.
    22.4 Dashboard
    Some dashboard values are still sample data.
    Dashboard should be connected to receipt database queries.
    22.5 Settings
    Settings are still in memory.
    Language selection does not translate the full app yet.
    Receipt footer is not persisted yet unless later connected to Room.
    22.6 Printing
    Real Bluetooth printing is not implemented yet.
    Current printer returns placeholder error/result.
    22.7 Cloud Sync
    Cloud backup/sync is not implemented.
    Firebase is planned for future version, not current core version.
    22.8 Testing
    No full automated test suite yet.
    Most testing is currently manual.
23. Manual Testing Checklist
    23.1 Login Tests
    Admin login works.
    Barista login works.
    Wrong PIN shows an error.
    Logout returns user to login.
    23.2 Role-Based Navigation Tests
    Admin sees Sales, Dashboard, Inventory, Receipts, Settings.
    Barista sees Sales and Settings only.
    Admin screens are hidden from Barista.
    Selected navigation item is highlighted.
    23.3 Sales Tests
    Products load from Room.
    Categories appear correctly.
    Category filtering works.
    Product card tap adds item to cart.
    Tapping same product increases quantity.
    Quantity + button works.
    Quantity - button works.
    Quantity reaching zero removes item.
    Clear Ticket works before payment.
    Cancel Order opens confirmation.
    Cancel Order clears active order.
    23.4 Payment Tests
    Charge button is disabled when cart is empty.
    Charge button opens payment method dialog.
    Cash payment validates amount received.
    Cash payment calculates change.
    Card payment confirms successfully.
    Mobile payment confirms successfully.
    Receipt preview opens after payment.
    Paid order cannot be edited.
    23.5 Receipt Tests
    Receipt preview shows correct products.
    Receipt preview shows correct subtotal/tax/total.
    Receipt preview shows payment method.
    Receipt preview shows amount paid and change for cash.
    Receipt footer reflects admin settings for current session.
    Print button shows printer availability message.
    Next Order saves receipt to Room.
    Next Order clears current order.
    23.6 Receipt History Tests
    Admin can open Receipts screen.
    Saved receipts appear after completing orders.
    Receipt number/date/payment/total are shown.
    Tapping a receipt opens detail dialog if Step 24 is implemented.
    Receipt detail dialog shows product lines.
    Barista cannot see Receipts screen.
    23.7 Dashboard Tests
    Dashboard opens for Admin.
    Barista cannot access Dashboard.
    Summary cards appear.
    Charts appear.
    Tap Top Products chart opens details.
    Tap Payments chart opens details.
    Tap Low Stock chart opens details.
    Low stock count matches inventory low stock items.
    23.8 Inventory Tests
    Inventory opens for Admin.
    Barista cannot access Inventory.
    Product list appears.
    Low stock items are highlighted.
    Stock quantities and reorder levels are visible.
    23.9 Settings Tests
    Admin sees language and receipt footer settings.
    Barista sees language only.
    Admin footer change affects new receipt preview.
    Language buttons visually toggle.
24. Recommended Next Steps

This section lists the best order to continue development after this pause.

Step 1 — Verify Latest Receipt Detail Implementation

Check whether the latest receipt detail feature has been fully applied.

Verify:

ReceiptDetailItem.kt exists.
ReceiptDao.getReceiptDetailsOnce() exists.
ReceiptRepository.getReceiptDetailsOnce() exists.
ReceiptHistoryViewModel has selected receipt state.
ReceiptHistoryScreen supports tapping a receipt.
SalesScreen.kt passes detail state/actions into ReceiptHistoryScreen.

Then test:

Create order → save receipt → open Receipts → tap receipt → view product lines.
Step 2 — Make Dashboard Use Real Receipt Data

Replace sample dashboard values with Room queries.

Needed queries:

Total sales.
Number of orders.
Average order value.
Payment breakdown grouped by method.
Top products grouped by product name.

Likely DAO additions:

SELECT SUM(total) FROM receipt_headers
SELECT COUNT(*) FROM receipt_headers
SELECT payment_method, COUNT(*), SUM(total) FROM receipt_headers GROUP BY payment_method
SELECT product_name, SUM(quantity), SUM(line_total) FROM receipt_details GROUP BY product_name
Step 3 — Fix Receipt Numbering Permanently

Move receipt sequence generation from memory to database.

Options:

Generate receipt number after insert using receipt ID.
Store last receipt sequence in Settings table.
Query latest receipt count/ID and generate next number.

Recommended simple approach:

Use receipt ID or count from database to generate next receipt number.
Step 4 — Deduct Inventory Stock After Sale

When receipt is saved, stock quantity should decrease.

Example:

Before sale: Latte stock = 28
Sale: 2 Latte
After sale: Latte stock = 26

This will require product ID tracking in receipt details or mapping product names carefully.

Better future design: receipt details should store product_id as well as product_name.

Step 5 — Add Admin Product Management

Inventory should become editable.

Admin should be able to:

Add product.
Edit product name/category/price/stock/reorder level.
Disable product.
Possibly delete product if no sales history exists.
Step 6 — Save Settings to Room

Create SettingsEntity.

Save:

Selected language.
Receipt footer text.
Possibly company details later.
Step 7 — Add Room-Backed Users

Create:

UserEntity
UserDao
UserRepository
LoginViewModel

Support:

Admin default user seeding.
Barista account creation.
PIN hash.
Role.
Active/inactive status.
Step 8 — Add Company Table

Create CompanyEntity.

Store:

Company name.
Address lines.
Country.
BRN.
VAT.
Phone.
Email.

Current Version 1 decision: company details are not editable from Settings, but should still exist in the database later.

Step 9 — Improve Receipt History

Add:

Search by receipt number.
Filter by date.
Filter by payment method.
Reprint receipt.
View full receipt preview from saved data.
Export receipt PDF.
Step 10 — Real Printer Integration

Implement:

Bluetooth device discovery/selection.
Save selected printer.
Connect to paired printer.
Send ESC/POS bytes.
Handle printer unavailable errors.
Step 11 — Improve Architecture

Refactor large files into smaller components.

Recommended:

MainScreen.kt
SideNavigation.kt
SalesScreen.kt
ProductArea.kt
TicketPanel.kt
DashboardScreen.kt
InventoryScreen.kt
ReceiptHistoryScreen.kt
SettingsScreen.kt
Step 12 — Documentation and Report Updates

Update coursework report with:

Current architecture.
Room database design.
Screenshots.
Role-based navigation.
Receipt saving workflow.
Limitations.
Future work.
25. Suggested Current Milestone Commit Tags

Useful commit checkpoints already covered or recommended:

Add payment screens for cash card and mobile
Improve login screen responsiveness and keyboard handling
Improve ticket panel UI design
Improve payment method dialog UI
Add generate receipt action after payment confirmation
Add receipt preview dialog and next order flow
Add dynamic receipt number and timestamp
Refactor receipt preview to use receipt model
Improve thermal receipt preview action layout
Add fake receipt printer service layer
Add ESC POS receipt formatter
Add Bluetooth printer support skeleton
Add main screen navigation
Add settings screen
Add inventory screen
Add dashboard screen
Add dashboard charts and detail dialogs
Add Room database dependencies
Add product database table
Add product database provider and repository
Connect product data to Room
Add receipt database tables
Save completed receipts to Room
Add receipt history screen
Add receipt detail view
26. Glossary for Non-Developers
    Kotlin

The programming language used to build the Android app.

Jetpack Compose

Android's modern UI toolkit. Instead of XML layouts, screens are built using Kotlin functions called Composables.

Composable

A function that draws part of the UI. Example: a button, card, screen, or dialog.

Room

Android library that makes it easier to use SQLite databases.

SQLite

A local database stored on the device.

Entity

A Kotlin class that represents a database table.

DAO

Data Access Object. Contains database queries.

Repository

A clean layer between the database and the rest of the app.

ViewModel

A class that holds UI data and business logic so the UI does not directly manage everything.

Flow

A Kotlin data stream. When database data changes, the UI can automatically receive updates.

StateFlow

A Flow that always has a current value and is useful for UI state.

KSP

Kotlin Symbol Processing. Used by Room to generate database code automatically.

ESC/POS

A command format used by many thermal receipt printers.

Destructive Migration

A development shortcut where Room deletes and recreates tables when the database structure changes. Useful during development, unsafe for production data.

27. Final Handover Notes

The project is currently in a strong intermediate state. The UI is already functional, and the app has moved from pure sample data toward a real Room-backed architecture.

The most important thing now is to avoid rushing into many new features at once. The recommended path is:

1. Verify receipt saving and receipt detail view.
2. Make dashboard use real receipt data.
3. Fix receipt numbering using database state.
4. Deduct stock after completed sale.
5. Add product management.
6. Add user management.
7. Save settings permanently.
8. Refactor large UI files.
9. Polish and document for submission.

Farhaan and Christa should coordinate through GitHub using feature branches and clear commit messages. Before making major structural changes, both collaborators should pull the latest code and agree on who is editing which package/screen.

This DEV_LOG should be updated after every major feature or debugging checkpoint.




## 2026-10-07 — Pricing and discounts (Room 10)

- Category management was merged into `develop/pos-improvements` through PR #3
  (`7ed5b14`); pricing work starts from that integration commit.
- Added Long-cent money handling across products, cart, payments, receipt models,
  history, PDF/thermal output and dashboard aggregates. Updated seed prices.
- Removed additional VAT from new sales; added whole-order role-limited discounts
  (BARISTA 0/5%, ADMIN 0/5/10/15%) and explicit HALF_UP rounding.
- Persisted receipt percentage/VAT flags and exact line discount allocations for
  net dashboard reporting. Historical monetary values remain preserved.
- Registered non-destructive Room 9 -> 10 migration; removed destructive fallback.
  Legacy migration gaps before version 8 remain documented, not fabricated.
- Validation: six standalone Kotlin/JUnit tests and two host SQLite migration/query
  tests passed. Full Gradle build attempted but blocked at distribution download
  by Java network connectivity. Android/Room/device validation remains pending.
- See `docs/PRICING.md` for the money contract, upgrade limits and pre-merge checklist.
  No feature merge is authorized by this entry; owner approval is still required.
