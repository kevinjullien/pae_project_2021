import callAPI from "../utils/api";
import {RedirectUrl} from "./Router";
import {getUserSessionData} from "../utils/session.js";
import PrintError from "../utils/PrintError";
import Navbar from "./Navbar";
import WaitingSpinner from "../utils/WaitingSpinner.js"
import {convertDateTimeToStringDate} from "../utils/tools.js";
import {FurniturePage} from "./FurniturePage.js";

const API_BASE_URL = "/api/visits/";
let page = document.querySelector("#page");
let userData;
let listVisitsWaiting;

let VisitsPage = () => {
    userData = getUserSessionData();
    let menu = `
    <div class="menuAdmin">
        <div class="condensed small-caps menuAdminOn" id="visits">Visites en attente</div>
        <div class="condensed small-caps" id="visitsToBeProcessed">Visites à traiter</div>
        <div class="condensed small-caps" id="advancedSearches">Recherches avancées</div>
        <div class="condensed small-caps" id="confirmRegister">Confirmation des inscriptions</div>
    </div>
    `;
    let visitPage = `<div class="visits-title small-caps">
    <div class="all-furn-title small-caps">Visites en attente</div>
    </div>`;
    page.innerHTML = menu + visitPage;

    let advancedSearches = document.getElementById("advancedSearches");
    advancedSearches.addEventListener("click", onAdvancedSearches);

    let confirmRegister = document.getElementById("confirmRegister");
    confirmRegister.addEventListener("click", onConfirmRegister);

    let btnWaiting = document.getElementById("visits");
    btnWaiting.addEventListener("click", onVisitsWaiting);

    let btnToTreat = document.getElementById("visitsToBeProcessed");
    btnToTreat.addEventListener("click", onVisitsToBeProcessed);
    onVisitsWaiting();

};

const onVisits = (e) => {
    e.preventDefault();
    RedirectUrl("/visits");
};

const onVisitsToBeProcessed = (e) => {
    e.preventDefault();
    RedirectUrl("/visitsToBeProcessed");
}

const onAdvancedSearches = (e) => {
    e.preventDefault();
    RedirectUrl("/advancedSearches");
};

const onConfirmRegister = (e) => {
    e.preventDefault();
    RedirectUrl("/confirmRegistration");
};

const onVisitsWaiting = async () => {
    try {
        listVisitsWaiting = await callAPI(
            API_BASE_URL + "notConfirmedVisits",
            "GET",
            userData.token,
            undefined,
        );
    } catch (err) {
        if (err == "Error: Admin only") {
            err.message = "Seuls les administrateurs peuvent accéder à cette page !";
        }
        console.error("VisitsPage::onVisitsWaiting", err);
        PrintError(err);
    }
    let visitsWaiting = `
    <div class="visitsWaiting">
        <table class="table table-light">
            <thead>
                <tr>
                    <th scope="col">Date de la demande</th>
                    <th scope="col">Client</th>
                    <th scope="col">Nombres de meubles</th>
                    <th scope="col">Adresse</th>
                    <th scope="col"></th>
                </tr>
            </thead>
        <tbody class="eachVisit">
    `;
    visitsWaiting += listVisitsWaiting
        .map((visit) =>
            `<tr>
                 <td>${convertDateTimeToStringDate(visit.requestDateTime)}</td>
                <td>${visit.client.firstName} ${visit.client.lastName}</td>
                <td>${visit.amountOfFurnitures}</td>
                <td><p class="block-display">${visit.warehouseAddress.street} ${visit.warehouseAddress.buildingNumber} ${(visit.warehouseAddress.unitNumber == null ? "" : "/" + visit.warehouseAddress.unitNumber)}<br>
                    ${visit.warehouseAddress.postCode} - ${visit.warehouseAddress.city} <br>
                    ${visit.warehouseAddress.country}</p></td>
                <td><button name="trigger_popup_fricc" class="btn btn-dark condensed small-caps block-display" data-id="${visit.idRequest}" type="submit">Consulter la demande de visite</button></td>
            </tr>
            `
        ).join("");

    page.innerHTML += visitsWaiting;
    page.innerHTML += `</tbody></table></div>`;
    let listVisit = document.getElementsByName("trigger_popup_fricc");
    Array.from(listVisit).forEach((e) => {
        e.addEventListener("click", onClickVisit);
    });
    

    let visits = document.getElementById("visits");
    visits.addEventListener("click", onVisits);

    let visitsATraiter = document.getElementById("visitsToBeProcessed");
    visitsATraiter.addEventListener("click", onVisitsToBeProcessed);

    let advancedSearches = document.getElementById("advancedSearches");
    advancedSearches.addEventListener("click", onAdvancedSearches);
    
    let confirmRegister = document.getElementById("confirmRegister");
    confirmRegister.addEventListener("click", onConfirmRegister);
}


const onClose = (e) => {
    e.preventDefault();
    let idVisit;
    if (e.srcElement.dataset.idrequest){
        idVisit = e.srcElement.dataset.idrequest;
    }else{
        idVisit = e.srcElement.dataset.id;
    }
    

    Array.from(document.getElementsByClassName("hover_bkgr_fricc")).forEach((element) => {
        if (element.dataset.id == idVisit) {
            element.style.display = "none";
            return;
        }
    });
}

async function onClickVisit(e) {
    let idVisit = e.srcElement.dataset.id;
    let visit = listVisitsWaiting.filter(e => e.idRequest == idVisit)[0];
    let popupVisit = `
    <div class="hover_bkgr_fricc" data-id="${visit.idRequest}">
        <span class="helper"></span>
        <div>
            <div class="popupCloseButton" data-id="${visit.idRequest}">
                &times;
            </div>
            <h2>Confirmer la visite ?</h2>
            <div>
                <span class="titre">Plage horaire : </span> ${visit.timeSlot}<br>
                <h4>Adresse : </h4>
                <div>${visit.warehouseAddress.street} ${visit.warehouseAddress.buildingNumber} ${(visit.warehouseAddress.unitNumber == null ? "" : "/" + visit.warehouseAddress.unitNumber)}<br>
                    ${visit.warehouseAddress.postCode} - ${visit.warehouseAddress.city} <br>
                    ${visit.warehouseAddress.country} 
                </div><br>
                <h4>Meuble(s) : </h4><br>
                <div id="allFurnitures"></div>
                <h4>Date de la visite</h4><input type="datetime-local" class="scheduledDateTime" id="scheduledDateTime${visit.idRequest}" min="${Date.now}" max="9999-31-12T23:59">
                <h4>Motif du refus : </h4><textarea class="explanatoryNote" id="explanatoryNote${visit.idRequest}"></textarea><br>
                <div class="container">
                    <div class="row">
                        <button class="btn btn-outline-success col-6 confirmVisitBtn" name="confirmBtn" data-id="${visit.idRequest}" type="submit">Confirmer</button>
                        <button class="btn btn-outline-danger col-6 cancelVisitBtn" name="cancelBtn" data-id="${visit.idRequest}" type="submit">Refuser</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    `;

    document.getElementById("popups").innerHTML = popupVisit;
    WaitingSpinner(document.getElementById("allFurnitures"))
    Array.from(document.getElementsByClassName("hover_bkgr_fricc")).forEach((element) => {
        if (element.dataset.id == idVisit) {
            element.style.display = "block";
            return;
        }
    });

    Array.from(document.getElementsByClassName("popupCloseButton")).forEach((e) => {
        e.addEventListener("click", onClose);
    });

    Array.from(document.getElementsByClassName("confirmVisitBtn")).forEach((e) => {
        e.addEventListener("click", onConfirm);
    });

    Array.from(document.getElementsByClassName("cancelVisitBtn")).forEach((e) => {
        e.addEventListener("click", onCancel);
    });

    let allFurnitures = document.getElementById("allFurnitures");
    let toAdd = `
        <div class="allFurnituresForOnVisit">
            <ol>
    `;
    let listFurnituresForOnVisit;
    try{
        listFurnituresForOnVisit = await callAPI(
            API_BASE_URL + idVisit + "/furnitures",
            "GET",
            userData.token,
            undefined,
        )
    }catch (err) {
        if (err == "Error: Admin only") {
            err.message = "Seuls les administrateurs peuvent accéder à cette page !";
        }
        console.error("VisitsPage::onVisitsWaiting", err);
        PrintError(err);
    }

    listFurnituresForOnVisit
        .map((furniture) => {
            toAdd += `
                <li>
                    <div class="furniture" id="${furniture.id}" data-id="${furniture.id}">
                        <div class="toClick" data-id="${furniture.id}" data-idrequest="${visit.idRequest}">${furniture.description}</div>
                        <div class="photoDiv">`;
            furniture.listPhotos.map(e => {
                toAdd += `<img class="imageVisits" src="${e.photo}">`
            });
                        toAdd +=  `</div>
                    </div>
                </li>
            `
        });
    toAdd += `</ol></div>`;

    

    allFurnitures.innerHTML = toAdd;
    
    let listDesFurnitures = document.getElementsByClassName("toClick");
    Array.from(listDesFurnitures).forEach((e) => {
        e.addEventListener("click", onFurniture);
    });

    
}


const onConfirm = async (e) => {
    let id = e.srcElement.dataset.id;
    let scheduledDateTime = document.getElementById('scheduledDateTime'+id).value;
    if (scheduledDateTime == "") {
        let error = {
            message: "Veuillez d'abord entrer une date et heure de visite",
        }
        PrintError(error);
        return;
    }else{
        Array.from(document.getElementsByClassName("hover_bkgr_fricc")).forEach((element) => {
            if (element.dataset.id == id) {
                element.style.display = "none";
                return;
            }
        });
    }
    try {
        await callAPI(
            API_BASE_URL + id + "/accept",
            "POST",
            userData.token,
            {
                scheduledDateTime: scheduledDateTime,
            },
        );
    } catch (err) {
        PrintError(err);
    }
    Navbar();
    VisitsPage();
}

const onCancel = async (e) => {
    let id = e.srcElement.dataset.id;
    let explanatoryNote = document.getElementById("explanatoryNote"+id).value;
    if (explanatoryNote == "") {
        let error = {
            message: "Veuillez d'abord entrer un motif expliquant la raison du refus",
        }
        PrintError(error);
        return;
    }else{
        Array.from(document.getElementsByClassName("hover_bkgr_fricc")).forEach((element) => {
            if (element.dataset.id == id) {
                element.style.display = "none";
                return;
            }
        });
    }
    try {
        await callAPI(
            API_BASE_URL + id + "/cancel",
            "POST",
            userData.token,
            {
                explanatoryNote: explanatoryNote,
            },
        );
    } catch (err) {
        PrintError(err);
    }
    VisitsPage();
}

const onFurniture = (e) => {
    let id = e.srcElement.dataset.id;
    onClose(e);
    FurniturePage(id);
};

export default VisitsPage;